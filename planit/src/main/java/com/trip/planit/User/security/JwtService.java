package com.trip.planit.User.security;

import com.trip.planit.User.entity.CookieRule;
import com.trip.planit.User.entity.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // 쿠키 만료 시간 (초 단위, 여기서는 1일)
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 86400;

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void addAccessTokenCookie(HttpServletResponse response, String userEmail, Role role) {
        String token = jwtUtil.generateToken(userEmail, role);

        response.setHeader("Set-Cookie",
            "accessToken=" + token + "; Path=/; Max-Age=86400; HttpOnly; SameSite=None; Secure");

        System.out.println("JwtService.addAccessTokenCookie: Set-Cookie = accessToken=" + token + "; Path=/; Max-Age=86400; HttpOnly; SameSite=None; Secure");
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from(CookieRule.ACCESS_PREFIX.getValue(), "")
                .httpOnly(true)
                .secure(false) // 개발환경
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", expiredCookie.toString());
    }
}
