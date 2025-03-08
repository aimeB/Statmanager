package com.genesis.api.statmanager.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("✅ Enregistrement du point d'entrée WebSocket : /ws");
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200") // 🚀 Autorise Angular
                .withSockJS(); // ✅ Active SockJS pour fallback si WebSocket échoue
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        log.info("✅ Configuration du message broker WebSocket");
        registry.enableSimpleBroker("/topic"); // ✅ Permet l'envoi en temps réel
        registry.setApplicationDestinationPrefixes("/app"); // ✅ Préfixe pour envoyer des messages
    }
}
