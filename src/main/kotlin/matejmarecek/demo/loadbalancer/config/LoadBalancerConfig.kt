package matejmarecek.demo.loadbalancer.config

import matejmarecek.demo.loadbalancer.core.Algorithm

/**
 * Immutable interface for Load Balancer Configuration.
 * It is provided to objects that need read-only access.
 */
interface LoadBalancerConfig {
    val algorithm: Algorithm
    val initNumbOfProviders: Int
    val maxProviderRequests: Int
    val heartBeatSec: Int
    val heartBeatCheckTimeoutSec: Int
}