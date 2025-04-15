package com.trip.planit.User.config;

import com.trip.planit.User.controller.CustomHandshakeInterceptor;
import com.trip.planit.User.controller.CustomHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 엔드포인트 등록, SockJS fallback 활성화
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:63342")
                .addInterceptors(new CustomHandshakeInterceptor()) // 인증 정보를 attributes에 저장
                .setHandshakeHandler(new CustomHandshakeHandler())    // 저장된 principal을 반환하도록 함
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic");    // 구독 경로
        registry.setApplicationDestinationPrefixes("/pub");   // 발행 경로
        registry.setUserDestinationPrefix("/user");           // 사용자 대상 경로
    }
}
