package matejmarecek.demo.loadbalancer.provider

import matejmarecek.demo.loadbalancer.config.provider.DefaultProviderConfig
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.random.Random

/**
 * Default implementation of the Provider.
 * It is used to simulate the work (using delay) and failures of real Providers.
 *
 * The Providers can be configured at runtime using [DefaultProviderConfig]
 */
class DefaultProvider @Autowired constructor(private val config: DefaultProviderConfig) : Provider {

    override val id = generate()
    private var lastFail : Long = 0

    override suspend fun get() : String {
        randomDelay(config.minGetDelayMs, config.maxGetDelayMs)
        return id
    }

    override suspend fun check() : Boolean {
        randomDelay(config.minCheckDelayMs, config.maxCheckDelayMs)

        val timeSinceLastFail = System.currentTimeMillis() - lastFail
        val isTimeToRecover = timeSinceLastFail > config.recoverTimeMs
        if (!isTimeToRecover) {
            return false
        }

        val failed = Random.nextInt(0, 100) < config.checkFailPercentage
        if (failed) {
            lastFail = System.currentTimeMillis()
        }
        return !failed
    }

    private suspend fun randomDelay(from : Long, to : Long) {
        val canDoDelay = from in 0 until to
        if (canDoDelay) {
            delay(Random.nextLong(from, to))
        }
    }

    // Generator of IDs pro the Providers
    private companion object IdGenerator {
        private val idCounter = AtomicLong()
        fun generate() = "${DefaultProvider::class.java.simpleName}: ${idCounter.getAndIncrement()}"
    }
}