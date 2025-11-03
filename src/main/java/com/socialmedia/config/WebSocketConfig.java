package com.socialmedia.config;

//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//	@Override
//	public void configureMessageBroker(MessageBrokerRegistry config) {
//		// Client can subscribe to topics prefixed with "/topic"
//		config.enableSimpleBroker("/topic");
//
//		// Messages from client with "/app" prefix will be routed to @MessageMapping
//		// methods
//		config.setApplicationDestinationPrefixes("/app");
//	}
//
//	@Override
//	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		// WebSocket endpoint URL (Angular will connect to this)
//		registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS(); // fallback for older browsers
//	}
//}
