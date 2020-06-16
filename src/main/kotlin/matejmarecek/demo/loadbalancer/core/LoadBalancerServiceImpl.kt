package matejmarecek.demo.loadbalancer.core

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import matejmarecek.demo.loadbalancer.config.LoadBalancerConfig
import matejmarecek.demo.loadbalancer.config.provider.DefaultProviderConfig
import matejmarecek.demo.loadbalancer.provider.DefaultProvider
import matejmarecek.demo.loadbalancer.provider.Provider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PreDestroy
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random

/**
 * An implementation of the [LoadBalancerService].
 */
@Service
class LoadBalancerServiceImpl @Autowired constructor(private val config: LoadBalancerConfig,
                                                     private val defaultProviderConfig: DefaultProviderConfig)
    : LoadBalancerService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val ongoingRequests = AtomicInteger(0)
    val maxNumbOfProviders = 10

    private val algorithm: Algorithm
        get() = config.algorithm


    private val providersMutex = Mutex()
    private val liveProviders: MutableList<Provider> = (1..config.initNumbOfProviders)
            .map { DefaultProvider(defaultProviderConfig) }
            .toMutableList()

    private var brokenProviders = listOf<Provider>()
    private var recoveredProviders = listOf<Provider>()

    private val heartBeatTimer: Timer

    init {
        val heartBeatMs = config.heartBeatSec * 1000L
        heartBeatTimer = fixedRateTimer(name = "heathCheckTimer", initialDelay = heartBeatMs, period = heartBeatMs) {
            runCheck()
        }
    }

    @PreDestroy
    fun destroy() {
        heartBeatTimer.cancel()
    }


    @Throws(TooManyRequestsException::class, NoProviderAvailableException::class)
    override fun get(): String {
        val provider = when (algorithm) {
            Algorithm.RANDOM -> selectProviderRandom()
            Algorithm.ROUND_ROBIN -> selectProviderRoundRobin()
        } ?: throw NoProviderAvailableException("Could not allocate a healthy Provider.")

        return callProviderGet(provider)
    }

    @Throws(TooManyRequestsException::class)
    private fun callProviderGet(provider: Provider): String {
        val maxRequests = synchronized(liveProviders) { liveProviders.size } * config.maxProviderRequests
        if (ongoingRequests.get() >= maxRequests) {
            throw TooManyRequestsException("Too many ongoing requests!")
        }

        ongoingRequests.incrementAndGet()
        try {
            return runBlocking { provider.get() }
        } catch (ex: Exception) {
            throw ex
        } finally {
            ongoingRequests.decrementAndGet()
        }
    }

    // -------------------------------------
    // --- Provider selection algorithms ---
    // -------------------------------------
    private fun selectProviderRandom(): Provider? {
        synchronized(liveProviders) {
            if (liveProviders.isEmpty()) {
                return null
            }
            val index = Random.nextInt(0, liveProviders.size)
            return liveProviders[index]
        }
    }

    private var roundRobinIndex = 0
    private fun selectProviderRoundRobin(): Provider {
        synchronized(liveProviders) {
            roundRobinIndex = (roundRobinIndex + 1) % liveProviders.size
            return liveProviders[roundRobinIndex]
        }
    }

    // -----------------------------
    // --- Provider health check ---
    // -----------------------------
    private fun runCheck() = runBlocking {
        providersMutex.withLock {
            val timeout = config.heartBeatCheckTimeoutSec * 1000L
            val providersCopy = synchronized(liveProviders) { liveProviders.toList() }

            val deferredInUseCheck = async { providersCopy.check(timeout) }
            val deferredBrokenCheck = async { brokenProviders.check(timeout) }
            val deferredRecoveredCheck = async { recoveredProviders.check(timeout) }

            val inUseCheck = deferredInUseCheck.await()
            val brokenCheck = deferredBrokenCheck.await()
            val recoveredCheck = deferredRecoveredCheck.await()

            brokenProviders = inUseCheck.getBroken() + brokenCheck.getBroken() + recoveredCheck.getBroken()
            recoveredProviders = brokenCheck.getHealthy()

            val notIncluded = excludeIncludeLiveProviders(inUseCheck.getBroken(), recoveredCheck.getHealthy())
            if (notIncluded.isNotEmpty()) {
                logger.warn("Not enough space to include recovered Providers: ${notIncluded.joinToString(", ") { it.id }}")
                recoveredProviders = recoveredProviders + notIncluded
            }
        }
    }

    /**
     * Excludes/includes Providers from/in the list of Live Providers.
     *
     * @param [toExclude] providers to be excluded form the list of Live Providers
     * @param [toInclude] providers to be included form the list of Live Providers
     * @return list of providers that could not have been included. Most likely because of capacity issues
     */
    private fun excludeIncludeLiveProviders(toExclude: List<Provider>, toInclude: List<Provider>): List<Provider> {
        synchronized(liveProviders) {
            toExclude.forEach { excludeProviderFromLive(it) }

            data class IncludeOp(val provider: Provider, val result: IncludeResult)

            return toInclude.map { IncludeOp(it, includeProviderRaw(it)) }
                    .filter { it.result != IncludeResult.INCLUDED }
                    .map { it.provider }
        }
    }

    private fun excludeProviderFromLive(provider: Provider): Boolean {
        return synchronized(liveProviders) {
            liveProviders.removeIf { p -> p.id == provider.id }
        }
    }

    override suspend fun includeProvider(provider: Provider): IncludeResult {
        providersMutex.withLock {
            return includeProviderRaw(provider)
        }
    }

    private fun includeProviderRaw(provider: Provider): IncludeResult {
        synchronized(liveProviders) {
            if (liveProviders.size == maxNumbOfProviders) {
                return IncludeResult.NOT_ENOUGH_SPACE
            }

            val alreadyIncluded = liveProviders.any { p -> p.id == provider.id }
            if (!alreadyIncluded) {
                liveProviders.add(provider)
            }

            return if (alreadyIncluded) IncludeResult.ALREADY_INCLUDED else IncludeResult.INCLUDED
        }
    }

    override suspend fun excludeProvider(provider: Provider): Boolean {
        providersMutex.withLock {
            return excludeProviderRaw(provider)
        }
    }

    private fun excludeProviderRaw(provider: Provider): Boolean {
        val removedFromLive = excludeProviderFromLive(provider)
        val removeFromBroken = brokenProviders.any { it.id == provider.id }
        if (removeFromBroken) {
            brokenProviders = brokenProviders.filter { it.id != provider.id }
        }
        val removedFromRecovered = recoveredProviders.any { it.id == provider.id }
        if (removeFromBroken) {
            recoveredProviders = recoveredProviders.filter { it.id != provider.id }
        }

        return removedFromLive || removeFromBroken || removedFromRecovered
    }


    /**
     * This method does not do any synchronization. For sake of speed,
     * it returns the raw state as is (the state can be temporarily inconsistent).
     *
     * @return the current state of the Load Balancer
     */
    override val state: LoadBalancerState
        get()  {
            return LoadBalancerState(
                    ongoingRequests = ongoingRequests.get(),
                    liveProviders = liveProviders.map(Provider::id),
                    brokenProviders = brokenProviders.map(Provider::id),
                    recoveredProviders = recoveredProviders.map(Provider::id)
            )
        }
}