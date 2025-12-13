package com.example.demo.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationHelper {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return Bearer 토큰 (없으면 null)
     */
    public String extractToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        
        // "Bearer " 또는 "Bearer Bearer " 같은 경우 처리
        String trimmed = authorizationHeader.trim();
        if (!trimmed.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        
        // "Bearer " 제거
        String token = trimmed.substring(7).trim();
        
        // "Bearer Bearer ..." 같은 중복 경우 처리
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        
        return token;
    }
    
    /**
     * Authorization 헤더에서 사용자 ID를 추출합니다.
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 사용자 ID (토큰이 유효하지 않으면 null)
     */
    public Long getUserIdFromHeader(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null) {
            return null;
        }
        
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}

