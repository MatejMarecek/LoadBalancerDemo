package matejmarecek.demo.loadbalancer.config.provider

/**
 * Public mutable version of the Default Provider Configuration.
 * It is for used (de)serialization and exposes configuration parts that can me modified at runtime.
 */
class DefaultProviderPublicConfig {
    var minGetDelayMs : Long? = null
    var maxGetDelayMs : Long? = null
    var minCheckDelayMs : Long? = null
    var maxCheckDelayMs : Long? = null
    var checkFailPercentage : Int? = null
    var recoverTimeMs : Long? = null
}