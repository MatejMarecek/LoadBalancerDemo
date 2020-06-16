package matejmarecek.demo.loadbalancer.provider

/**
 * Common interface for all Providers.
 */
interface Provider {
    val id : String
    suspend fun get() : String
    suspend fun check() : Boolean
}