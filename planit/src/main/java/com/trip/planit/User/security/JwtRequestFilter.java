package com.trip.planit.User.security;

import com.trip.planit.User.entity.CookieRule;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
                                    throws ServletException, IOException {

        // 기존 Authorization 헤더 대신 쿠키에서 JWT 토큰 읽기
        String jwt = "";
        if(request.getCookies() != null) {
            jwt = cookieUtil.resolveTokenFromCookie(request.getCookies(), CookieRule.ACCESS_PREFIX);
        } else {
            System.out.println("쿠키가 존재하지 않습니다.");
        }

        String email = null;
        if (jwt != null && !jwt.isEmpty()) {
            try {
                email = jwtUtil.extractemail(jwt);
                // 토큰 만료 시간 출력 (디버깅 용도)
                Date expirationDate = jwtUtil.getExpirationDateFromToken(jwt);
                System.out.println("Token Expiration Date: " + expirationDate);
            } catch (Exception e) {
                System.out.println("JWT에서 로그인 ID 추출 중 에러 발생: " + e.getMessage());
            }
        }

        // 로그로 현재 SecurityContext 인증 상태 확인
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current Authentication in SecurityContext: " + currentAuth);


        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    System.out.println("JWT 토큰이 유효합니다.");
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("사용자 인증 및 SecurityContext 설정 완료");
                } else {
                    System.out.println("JWT 토큰이 유효하지 않습니다.");
                }
            } catch (Exception e) {
                System.out.println("UserDetails 로드 혹은 토큰 검증 중 에러 발생: " + e.getMessage());
            }
        } else {
            System.out.println("로그인 ID가 null이거나 이미 인증이 설정됨");
        }

        filterChain.doFilter(request, response);
        System.out.println("필터 체인 진행됨");
    }


//    @Override
//    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
//        final String authorizationHeader = request.getHeader("Authorization");
//
//        String email = null;
//        String jwt = null;
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            jwt = authorizationHeader.substring(7);
//            try {
//                email = jwtUtil.extractemail(jwt);
//
//                // 토큰 만료 시간 출력
//                Date expirationDate = jwtUtil.getExpirationDateFromToken(jwt);
//                System.out.println("Token Expiration Date: " + expirationDate);
//
//            } catch (Exception e) {
//                System.out.println("Error extracting login ID from JWT: " + e.getMessage());
//            }
//        } else {
//            System.out.println("Authorization Header is either null or does not start with 'Bearer '");
//        }
//
//        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            try {
//                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
//
//                if (jwtUtil.validateToken(jwt, userDetails)) {
//                    System.out.println("JWT Token is valid");
//
//                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                            userDetails, null, userDetails.getAuthorities());
//                    usernamePasswordAuthenticationToken
//                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                    System.out.println("User authenticated and security context set");
//                } else {
//                    System.out.println("JWT Token is invalid");
//                }
//            } catch (Exception e) {
//                System.out.println("Error loading UserDetails or validating token: " + e.getMessage());
//            }
//        } else {
//            System.out.println("Login ID is null or Authentication is already set");
//        }
//
//        filterChain.doFilter(request, response);
//        System.out.println("Filter chain continued");
//    }
}

