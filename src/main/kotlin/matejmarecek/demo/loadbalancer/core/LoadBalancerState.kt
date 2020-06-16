package matejmarecek.demo.loadbalancer.core

/**
 * Read-only representation of the Load Balancer State.
 */
class LoadBalancerState (val ongoingRequests: Int,
                         val liveProviders : List<String>,
                         val brokenProviders : List<String>,
                         val recoveredProviders : List<String>)
