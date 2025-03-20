package com.trip.planit.User.security;

import com.trip.planit.User.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 86400000; // 1일

    // 서명키 생성
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    // 토큰 생성 (이메일과 역할을 포함)
    public String generateToken(String email, Role role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        System.out.println("Generating token. Current Time: " + now + ", Expiration Time: " + expiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", "ROLE_" + role.name()) // 예: "ROLE_USER"
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 이메일 추출
    public String extractEmail(String token) {
        String email = extractAllClaims(token).getSubject();
        System.out.println("Extracted Login ID from token: " + email);
        return email;
    }

    // 토큰에서 모든 클레임 추출 (Bearer 접두어 제거 포함)
    private Claims extractAllClaims(String token) {
        token = token.replace("Bearer ", "");
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 역할 추출 (생성 시 "role" 클레임으로 저장한 값 사용)
    public Role extractUserRole(String token) {
        Claims claims = extractAllClaims(token);
        String roleFromToken = (String) claims.get("role"); // 예: "ROLE_USER"
//        if (roleFromToken != null && roleFromToken.startsWith("ROLE_")) {
//            roleFromToken = roleFromToken.substring(5); // "USER"로 변환
//        }
        System.out.println("Extracted Role from token: " + roleFromToken);
        return Role.valueOf(roleFromToken);
    }

    // 토큰 유효성 확인 (이메일 일치 및 만료 여부)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        System.out.println("Validating token for username: " + username + ". Token is valid: " + isValid);
        return isValid;
    }

    // 토큰 만료 여부 확인
    private Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        boolean isExpired = expiration.before(new Date());
        System.out.println("Token expiration time: " + expiration + ". Is token expired: " + isExpired);
        return isExpired;
    }

    // 토큰 만료 시간 반환
    public Date getExpirationDateFromToken(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        System.out.println("Extracted expiration date from token: " + expiration);
        return expiration;
    }
}
