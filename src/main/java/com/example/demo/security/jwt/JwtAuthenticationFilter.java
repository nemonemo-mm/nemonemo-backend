package com.example.demo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            
            if (token != null && validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                
                // SecurityContext에 인증 정보 저장
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 토큰 검증 성공: userId={}", userId);
            }
        } catch (Exception e) {
            log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
            // 인증 실패 시 SecurityContext를 비우지 않고 그냥 통과
            // Spring Security의 .authenticated()가 401을 반환함
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7).trim();
            // "Bearer Bearer ..." 같은 중복 경우 처리
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
            return token;
        }
        
        return null;
    }

    /**
     * JWT 토큰이 유효한지 검증합니다.
     * 
     * @param token JWT 토큰
     * @return 유효 여부
     */
    private boolean validateToken(String token) {
        try {
            // 토큰 타입이 ACCESS인지 확인
            String tokenType = jwtTokenProvider.getTokenType(token);
            if (!"ACCESS".equals(tokenType)) {
                log.debug("ACCESS 토큰이 아닙니다: type={}", tokenType);
                return false;
            }
            
            // 토큰 검증 (만료, 서명 등)
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}

