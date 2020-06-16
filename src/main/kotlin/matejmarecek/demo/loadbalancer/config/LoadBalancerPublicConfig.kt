package matejmarecek.demo.loadbalancer.config

import matejmarecek.demo.loadbalancer.core.Algorithm

/**
 * Public mutable version of the Load Balancer Configuration.
 * It is for used (de)serialization and exposes configuration parts that can me modified at runtime.
 */
class LoadBalancerPublicConfig {
    var algorithm: Algorithm? = null
    var maxProviderRequests: Int? = null
}