package com.example.demo.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
}


