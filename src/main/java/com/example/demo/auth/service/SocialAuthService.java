package com.example.demo.auth.service;

import com.example.demo.auth.dto.AuthTokensResponse;
import com.example.demo.auth.dto.SocialLoginRequest;
import com.example.demo.auth.google.GoogleIdTokenVerifierService;
import com.example.demo.domain.entity.RefreshToken;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.enums.AuthProvider;
import com.example.demo.domain.enums.ClientType;
import com.example.demo.domain.repository.RefreshTokenRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final GoogleIdTokenVerifierService googleVerifier;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthTokensResponse loginWithGoogle(SocialLoginRequest request) throws GeneralSecurityException, IOException {
        String idToken = request.getIdToken();
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("AUTH_INVALID_TOKEN: ID 토큰이 없습니다.");
        }

        // 클라이언트 타입 로깅 (요청에서 제공된 경우)
        ClientType clientType = request.getClientType();
        if (clientType != null) {
            log.info("Google 로그인 요청 - 클라이언트 타입: {}", clientType);
        } else {
            log.info("Google 로그인 요청 - 클라이언트 타입: 미지정");
        }

        GoogleIdToken.Payload payload = googleVerifier.verify(idToken);
        if (payload == null) {
            throw new IllegalArgumentException("AUTH_INVALID_TOKEN: 토큰 검증 실패");
        }

        // ID 토큰에서 클라이언트 타입 자동 감지 (audience 확인)
        ClientType detectedClientType = googleVerifier.detectClientType(payload);
        if (detectedClientType != null) {
            if (clientType == null) {
                log.info("클라이언트 타입 자동 감지: {}", detectedClientType);
            } else if (clientType != detectedClientType) {
                log.warn("클라이언트 타입 불일치 - 요청: {}, 감지: {}", clientType, detectedClientType);
            } else {
                log.info("클라이언트 타입 확인: {}", detectedClientType);
            }
        } else if (clientType != null) {
            log.info("클라이언트 타입 (요청): {}", clientType);
        }

        String providerId = payload.getSubject();
        String email = payload.getEmail();

        // 기존 유저 존재 여부 확인 (먼저 조회)
        Optional<User> existingUserOpt = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, providerId);
        boolean isNewUser = existingUserOpt.isEmpty();

        User user;
        if (isNewUser) {
            // 새 사용자: name 필수 검증 후 회원가입
            String name = request.getName() != null ? request.getName() : (String) payload.get("name");
            String picture = request.getImageUrl() != null ? request.getImageUrl() : (String) payload.get("picture");

            // 이름 필수 검증 (새 사용자만)
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("AUTH_MISSING_NAME: 사용자 이름이 필요합니다.");
            }

            user = User.builder()
                    .email(email)
                    .name(name)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .imageUrl(picture)
                    .build();
            user = userRepository.save(user);
        } else {
            // 기존 사용자: name 검증 없이 바로 로그인
            user = existingUserOpt.get();
            
            // 선택적으로 name, imageUrl 업데이트 (request에 제공된 경우)
            String name = request.getName();
            String picture = request.getImageUrl();
            boolean updated = false;
            
            if (name != null && !name.trim().isEmpty() && !name.equals(user.getName())) {
                user.setName(name);
                updated = true;
            }
            
            if (picture != null && !picture.equals(user.getImageUrl())) {
                user.setImageUrl(picture);
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
            }
        }

        // 새 refresh token 발급 및 저장
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime refreshExpiry = LocalDateTime.now().plusSeconds(60L * 60 * 24 * 30); // 30일 (간단히 하드코딩)

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(refreshExpiry)
                .build();
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .imageUrl(user.getImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return AuthTokensResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .isNewUser(isNewUser)
                .user(userResponse)
                .build();
    }
}

