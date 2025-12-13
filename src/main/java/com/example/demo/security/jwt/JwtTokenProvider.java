package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;

    private final long accessTokenValidityMillis;
    private final long refreshTokenValidityMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:3600000}") long accessTokenValidityMillis,
            @Value("${jwt.refresh-token-validity-ms:2592000000}") long refreshTokenValidityMillis
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidityMillis = accessTokenValidityMillis;
        this.refreshTokenValidityMillis = refreshTokenValidityMillis;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityMillis, Map.of("type", "ACCESS"));
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityMillis, Map.of("type", "REFRESH"));
    }

    private String createToken(Long userId, long validityMillis, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰을 검증하고 Claims를 반환합니다.
     * 
     * @param token JWT 토큰
     * @return Claims 토큰이 유효한 경우
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws SecurityException 토큰 서명이 유효하지 않은 경우
     * @throws MalformedJwtException 토큰 형식이 잘못된 경우
     * @throws UnsupportedJwtException 지원하지 않는 토큰인 경우
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰의 타입(ACCESS/REFRESH)을 확인합니다.
     * 
     * @param token JWT 토큰
     * @return 토큰 타입 ("ACCESS" 또는 "REFRESH")
     */
    public String getTokenType(String token) {
        Claims claims = validateToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰이 만료되었는지 확인합니다 (예외 없이).
     * 
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        try {
            validateToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true; // 다른 예외도 만료로 간주
        }
    }

    /**
     * 토큰의 만료 시간을 반환합니다.
     * 
     * @param token JWT 토큰
     * @return 만료 시간 (Instant)
     */
    public Instant getExpirationFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration().toInstant();
    }

    /**
     * Refresh Token의 만료 시간(밀리초)을 반환합니다.
     * 
     * @return Refresh Token 만료 시간 (밀리초)
     */
    public long getRefreshTokenValidityMillis() {
        return refreshTokenValidityMillis;
    }
}


