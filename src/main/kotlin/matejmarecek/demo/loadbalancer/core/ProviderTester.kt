package matejmarecek.demo.loadbalancer.core

import matejmarecek.demo.loadbalancer.provider.Provider
import kotlinx.coroutines.withTimeout
import java.lang.Exception

/**
 * Contains functionality related to [Provider] health checking.
 */

data class ProviderCheck(val provider: Provider, val healthy: Boolean)

suspend fun List<Provider>.check(timeout : Long) : List<ProviderCheck> {
     return this.map { provider ->
        try {
            val healthy = withTimeout(timeout) { provider.check() }
            ProviderCheck(provider, healthy)
        } catch (ex: Exception) {
            ProviderCheck(provider, false)
        }
    }
}

fun List<ProviderCheck>.getHealthy() : List<Provider> {
    return this.filter { it.healthy }.map { it.provider }
}

fun List<ProviderCheck>.getBroken() : List<Provider> {
    return this.filter { !it.healthy }.map { it.provider }
}