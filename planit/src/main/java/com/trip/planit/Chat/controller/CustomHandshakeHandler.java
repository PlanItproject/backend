package com.trip.planit.Chat.controller;

import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // HandshakeInterceptor에서 저장한 principal을 반환
        Object principal = attributes.get("principal");
        if (principal instanceof Principal) {
            return (Principal) principal;
        }
        // 인증 정보가 없으면 null 반환
        throw new GeneralException(ErrorStatus.NOT_AUTHENTICATED);
    }
}
