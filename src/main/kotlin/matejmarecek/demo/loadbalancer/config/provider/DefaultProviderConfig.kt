package matejmarecek.demo.loadbalancer.config.provider

/**
 * Immutable interface for Default Provider Configuration.
 * It is provided to objects that need read-only access.
 */
interface DefaultProviderConfig {
    val minGetDelayMs: Long
    val maxGetDelayMs: Long
    val minCheckDelayMs: Long
    val maxCheckDelayMs: Long
    val checkFailPercentage: Int
    val recoverTimeMs: Long
}