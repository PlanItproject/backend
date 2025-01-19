package com.trip.planit.User.security;

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

    private static final long EXPIRATION_TIME = 86400000; // 일반 로그인 만료기간 : 1일

    // JWT 디코딩
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    // 토큰 생성
    public String generateToken(String email) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        System.out.println("Generating token. Current Time: " + now + ", Expiration Time: " + expiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now) // 발급 시간 설정
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String extractemail(String token) {
        String email = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody()
                .getSubject();
        System.out.println("Extracted Login ID from token: " + email);
        return email;
    }

    // token 유효성 확인을 위한
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractemail(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        System.out.println("Validating token for username: " + username + ". Token is valid: " + isValid);
        return isValid;
    }

    // Boolean - 토큰 반환
    private Boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        boolean isExpired = expiration.before(new Date());
        System.out.println("Token expiration time: " + expiration + ". Is token expired: " + isExpired);
        return isExpired;
    }

    // 토큰 만료 시간 반환
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        Date expiration = claims.getExpiration();
        System.out.println("Extracted expiration date from token: " + expiration);
        return expiration;
    }
}
