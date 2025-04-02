package com.trip.planit.User.controller;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // ** 고쳤음 **: HandshakeInterceptor에서 저장한 principal을 반환
        Object principal = attributes.get("principal");
        if (principal instanceof Principal) {
            return (Principal) principal;
        }
        // 인증 정보가 없으면 null 반환 (메시지 전송 시 문제가 발생할 수 있음)
        return null;
    }
}
