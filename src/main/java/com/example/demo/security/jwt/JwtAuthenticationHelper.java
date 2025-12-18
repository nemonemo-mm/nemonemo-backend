package com.example.demo.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationHelper {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * SecurityContext에서 인증된 사용자 ID를 가져옵니다.
     * JWT 필터가 설정한 인증 정보를 사용합니다.
     * 
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     * (레거시 지원용 - 가능하면 getCurrentUserId() 사용 권장)
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
     * (레거시 지원용 - 가능하면 getCurrentUserId() 사용 권장)
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 사용자 ID (토큰이 유효하지 않으면 null)
     */
    @Deprecated
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

