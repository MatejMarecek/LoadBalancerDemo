package matejmarecek.demo.loadbalancer.core

import matejmarecek.demo.loadbalancer.provider.Provider

enum class Algorithm { RANDOM, ROUND_ROBIN }

class NoProviderAvailableException(message: String?) : Exception(message) {}
class TooManyRequestsException(message: String?) : Exception(message) {}

enum class IncludeResult {
    INCLUDED,
    ALREADY_INCLUDED,
    NOT_ENOUGH_SPACE
}

/**
 * Interface for Load Balancer.
 */
interface LoadBalancerService {

    @Throws(TooManyRequestsException::class, NoProviderAvailableException::class)
    fun get(): String

    suspend fun includeProvider(provider: Provider): IncludeResult
    /**
     * Excludes the provider from the list of Live Providers
     * @return true if the element has been successfully removed; false if it was not present in the Load Balancer
     */
    suspend fun excludeProvider(provider: Provider): Boolean

    val state: LoadBalancerState
}
