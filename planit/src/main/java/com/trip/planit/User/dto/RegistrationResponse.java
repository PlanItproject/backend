package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {
    private boolean googleLogin;
    private String token;  // 구글 로그인인 경우 JWT 토큰 (필요 시)
}
