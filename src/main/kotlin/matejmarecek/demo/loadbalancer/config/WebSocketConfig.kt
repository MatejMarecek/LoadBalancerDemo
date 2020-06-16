package matejmarecek.demo.loadbalancer.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.*


/**
 * Performs configuration of web sockets.
 */

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/loadbalancer-websocket").setAllowedOrigins("*").withSockJS()
    }
}