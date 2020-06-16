package matejmarecek.demo.loadbalancer.config.provider

import matejmarecek.demo.loadbalancer.config.parseIntInLimit
import matejmarecek.demo.loadbalancer.config.parseLongInLimit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import kotlin.properties.Delegates

/**
 * Service responsible for Default Provider Configuration management.
 */
@Service
class DefaultProviderConfigService @Autowired constructor(private val env: Environment) : DefaultProviderConfig {
    override var minGetDelayMs by Delegates.notNull<Long>()
    override var maxGetDelayMs by Delegates.notNull<Long>()
    override var minCheckDelayMs by Delegates.notNull<Long>()
    override var maxCheckDelayMs by Delegates.notNull<Long>()
    override var checkFailPercentage by Delegates.notNull<Int>()
    override var recoverTimeMs by Delegates.notNull<Long>()

    init {
        resetToDefault()
    }

    final fun resetToDefault() {
        minGetDelayMs = parseLongInLimit(env["minRandomDelayMs"], 0, Long.MAX_VALUE, 25)
        maxGetDelayMs = parseLongInLimit(env["maxRandomDelayMs"], 0, Long.MAX_VALUE, 1200)
        minCheckDelayMs = parseLongInLimit(env["minCheckDelayMs"], 0, Long.MAX_VALUE, 5)
        maxCheckDelayMs = parseLongInLimit(env["maxCheckDelayMs"], 0, Long.MAX_VALUE, 1000)
        checkFailPercentage = parseIntInLimit(env["checkFailPercentage"], 0, 100, 7)
        recoverTimeMs = parseLongInLimit(env["recoverTimeMs"], 0, Long.MAX_VALUE, 15000)
    }

    fun trySetConfig(config: DefaultProviderPublicConfig) {
        minGetDelayMs = config.minGetDelayMs ?: minGetDelayMs;
        maxGetDelayMs = config.maxGetDelayMs ?: maxGetDelayMs;
        minCheckDelayMs = config.minCheckDelayMs ?: minCheckDelayMs;
        maxCheckDelayMs = config.maxCheckDelayMs ?: maxCheckDelayMs;
        checkFailPercentage = config.checkFailPercentage ?: checkFailPercentage;
        recoverTimeMs = config.recoverTimeMs ?: recoverTimeMs;
    }

    fun getPublicConfig(): DefaultProviderPublicConfig {
        val result = DefaultProviderPublicConfig()
        result.minGetDelayMs = minGetDelayMs
        result.maxGetDelayMs = maxGetDelayMs
        result.minCheckDelayMs = minCheckDelayMs
        result.maxCheckDelayMs = maxCheckDelayMs
        result.checkFailPercentage = checkFailPercentage
        result.recoverTimeMs = recoverTimeMs
        return result
    }
}