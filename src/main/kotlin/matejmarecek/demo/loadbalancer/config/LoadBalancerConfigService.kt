package matejmarecek.demo.loadbalancer.config

import matejmarecek.demo.loadbalancer.core.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import kotlin.properties.Delegates

/**
 * Service responsible for Load Balancer Configuration management.
 */

@Service
class LoadBalancerConfigService @Autowired constructor(private val env: Environment) : LoadBalancerConfig {
    override lateinit var algorithm: Algorithm
    override var initNumbOfProviders by Delegates.notNull<Int>()
    override var maxProviderRequests by Delegates.notNull<Int>()
    override var heartBeatSec by Delegates.notNull<Int>()
    override var heartBeatCheckTimeoutSec by Delegates.notNull<Int>()

    init {
        resetToDefault()
    }

    final fun resetToDefault() {
        algorithm = parseAlgorithm(env["algorithm"] ?: "random")
        initNumbOfProviders = parseIntInLimit(env["initNumbOfProviders"], 1, 10, 10)
        maxProviderRequests = parseIntInLimit(env["maxProviderRequests"], 1, 5, 5)
        heartBeatSec = parseIntInLimit(env["heartBeatSec"], 1, Int.MAX_VALUE, 7)
        heartBeatCheckTimeoutSec = parseIntInLimit(env["heartBeatCheckTimeoutSec"], 1, Int.MAX_VALUE, 12)
    }

    private fun parseAlgorithm(toParse: String): Algorithm {
        return try {
            Algorithm.valueOf(toParse)
        } catch (ex: IllegalArgumentException) {
            Algorithm.RANDOM
        }
    }

    fun trySetConfig(requestedConfig: LoadBalancerPublicConfig) {
        algorithm = requestedConfig.algorithm ?: algorithm
        maxProviderRequests = requestedConfig.maxProviderRequests ?: maxProviderRequests
    }

    fun getPublicConfig(): LoadBalancerPublicConfig {
        val result = LoadBalancerPublicConfig()
        result.algorithm = algorithm;
        result.maxProviderRequests = maxProviderRequests;
        return result
    }
}

