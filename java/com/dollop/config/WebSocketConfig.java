package com.dollop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 🔹 Enable broker for /topic (broadcasts) and /queue (private messages if
		// needed)
		config.enableSimpleBroker("/topic");

		// 🔹 Prefix for app destinations (when client sends messages)
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 🔹 This must match the SockJS endpoint in Angular: new
		// SockJS("http://localhost:8080/ws")
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*") // allow Angular dev server
				.withSockJS();
	}
}
