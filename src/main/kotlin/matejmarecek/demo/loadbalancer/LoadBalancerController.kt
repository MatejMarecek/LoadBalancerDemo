package matejmarecek.demo.loadbalancer

import matejmarecek.demo.loadbalancer.config.LoadBalancerConfigService
import matejmarecek.demo.loadbalancer.config.LoadBalancerPublicConfig
import matejmarecek.demo.loadbalancer.config.provider.DefaultProviderConfigService
import matejmarecek.demo.loadbalancer.config.provider.DefaultProviderPublicConfig
import matejmarecek.demo.loadbalancer.core.LoadBalancerService
import matejmarecek.demo.loadbalancer.core.LoadBalancerState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.fixedRateTimer

/**
 * The Load Balancer Controller that exposes the control and data to clients.
 */
@Controller
class LoadBalancerController {

    @Autowired
    private lateinit var balancerService: LoadBalancerService

    @Autowired
    private lateinit var loadBalancerConfigService: LoadBalancerConfigService

    @Autowired
    private lateinit var defaultProviderConfigService: DefaultProviderConfigService

    @Autowired
    private lateinit var webSocket: SimpMessagingTemplate

    lateinit var timer : Timer

    @PostConstruct
    fun postConstruct() {
        timer = fixedRateTimer(name = "periodicUpdateTimer", initialDelay = 100, period = 100) {
            sendStateUpdate()
        }
    }

    /**
     * Sends state update to all connected clients.
     */
    fun sendStateUpdate() {
        webSocket.convertAndSend("/topic/loadbalancer/state", balancerService.state)
    }

    @RequestMapping(value = ["/loadbalancer/state"], method = [RequestMethod.GET])
    @ResponseBody
    fun getLoadBalancerState(): LoadBalancerState {
        return balancerService.state
    }

    @PreDestroy
    fun destroy() {
        timer.cancel()
    }


    @MessageMapping("/get")
    @SendTo("/topic/get")
    @Throws(Exception::class)
    fun get(): String {
        return balancerService.get()
    }


    // -----------------------------------
    // --- Load Balancer configuration ---
    // -----------------------------------

    @RequestMapping(value = ["/loadbalancer/configuration"], method = [RequestMethod.GET])
    @ResponseBody
    fun getLoadBalancerConfig(): LoadBalancerPublicConfig {
        return loadBalancerConfigService.getPublicConfig()
    }

    @RequestMapping(value = ["/loadbalancer/configuration"], method = [RequestMethod.POST])
    @ResponseBody
    fun setLoadBalancerConfig(@RequestBody requestedConfig : LoadBalancerPublicConfig): LoadBalancerPublicConfig {
        loadBalancerConfigService.trySetConfig(requestedConfig)
        return loadBalancerConfigService.getPublicConfig()
    }

    @RequestMapping(value = ["/loadbalancer/configuration"], method = [RequestMethod.DELETE])
    @ResponseBody
    fun resetLoadBalancerConfig(): LoadBalancerPublicConfig {
        loadBalancerConfigService.resetToDefault()
        return loadBalancerConfigService.getPublicConfig()
    }


    // ------------------------------
    // --- Provider configuration ---
    // ------------------------------

    @RequestMapping(value = ["/provider/default/configuration"], method = [RequestMethod.GET])
    @ResponseBody
    fun getDefaultProviderConfig(): DefaultProviderPublicConfig {
        return defaultProviderConfigService.getPublicConfig()
    }

    @RequestMapping(value = ["/provider/default/configuration"], method = [RequestMethod.POST])
    @ResponseBody
    fun setDefaultProviderConfig(@RequestBody requestedConfig: DefaultProviderPublicConfig): DefaultProviderPublicConfig {
        defaultProviderConfigService.trySetConfig(requestedConfig)
        return defaultProviderConfigService.getPublicConfig()
    }

    @RequestMapping(value = ["/provider/default/configuration"], method = [RequestMethod.DELETE])
    @ResponseBody
    fun resetDefaultProviderConfig(): DefaultProviderPublicConfig {
        defaultProviderConfigService.resetToDefault()
        return defaultProviderConfigService.getPublicConfig()
    }
}