package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * WebSocket 설정
 * STOMP 프로토콜을 사용하여 실시간 메시징을 지원합니다.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 허용할 Origin 패턴
     * 환경 변수 WEBSOCKET_ALLOWED_ORIGINS로 설정 (쉼표로 구분)
     * 설정하지 않으면 모든 Origin 허용 (개발 환경용)
     */
    @Value("${websocket.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 메시지 브로커 설정
     * - /topic: 팀 단위 브로드캐스트 (1:N)
     * - /queue: 개인 메시지 (1:1)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독 경로: /topic, /queue
        config.enableSimpleBroker("/topic", "/queue");
        // 메시지 발행 경로: /app
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 설정
     * 클라이언트는 /ws로 WebSocket 연결을 시작합니다.
     * 프론트엔드에서는 현재 도메인을 기준으로 연결
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            // 환경 변수로 설정된 Origin만 허용 (프로덕션)
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            registry.addEndpoint("/ws")
                    .setAllowedOriginPatterns(origins.toArray(new String[0]))
                    .withSockJS();
        } else {
            // 환경 변수가 없으면 모든 Origin 허용 (개발 환경)
            registry.addEndpoint("/ws")
                    .setAllowedOriginPatterns("*")
                    .withSockJS();
        }
    }
}

