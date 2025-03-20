package com.trip.planit.User.security;

import com.trip.planit.User.entity.CookieRule;
import com.trip.planit.User.entity.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
        // JWT 토큰 생성 (이메일과 역할을 포함)
        String token = jwtUtil.generateToken(userEmail, role);

        // 쿠키 생성 (CookieRule.ACCESS_PREFIX의 값은 "access")
        Cookie jwtCookie = new Cookie(CookieRule.ACCESS_PREFIX.getValue(), token);
        jwtCookie.setHttpOnly(true);       // JavaScript 접근 차단
        jwtCookie.setSecure(true);         // HTTPS 환경에서만 전송 (운영 시 적용) //// ***** 서버 배포 시에는 true로 변경 ***
        jwtCookie.setPath("/");             // 애플리케이션 전체에 적용
        jwtCookie.setMaxAge(ACCESS_TOKEN_EXPIRATION_SECONDS); // 쿠키 만료시간 설정 (초 단위)

        // 응답에 쿠키 추가
        response.addCookie(jwtCookie);
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        // 쿠키 값을 null로 하고, 만료 시간을 0으로 설정하면 삭제됨
        Cookie clearCookie = new Cookie(CookieRule.ACCESS_PREFIX.getValue(), null);
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(true);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(clearCookie);
    }
}
