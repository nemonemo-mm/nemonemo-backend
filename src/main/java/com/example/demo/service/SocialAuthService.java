package com.example.demo.service;

import com.example.demo.dto.auth.AuthTokensResponse;
import com.example.demo.dto.auth.SocialLoginRequest;
import com.example.demo.domain.entity.RefreshToken;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.enums.AuthProvider;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.dto.team.TeamDetailResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.domain.entity.Team;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.DeviceTokenService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final FirebaseIdTokenVerifierService firebaseVerifier;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeamRepository teamRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeviceTokenService deviceTokenService;

    @Transactional
    public AuthTokensResponse loginWithGoogle(SocialLoginRequest request) throws FirebaseAuthException {
        String firebaseIdToken = request.getFirebaseIdToken();
        if (firebaseIdToken == null || firebaseIdToken.trim().isEmpty()) {
            throw new IllegalArgumentException("AUTH_INVALID_TOKEN: Firebase ID 토큰이 없습니다.");
        }

        // Firebase ID Token 검증
        FirebaseToken decodedToken = firebaseVerifier.verify(firebaseIdToken);
        if (decodedToken == null) {
            throw new IllegalArgumentException("AUTH_INVALID_TOKEN: 유효하지 않은 Firebase ID 토큰입니다.");
        }

        // Firebase UID 추출 (providerId로 사용)
        String firebaseUid = decodedToken.getUid();
        
        // 이메일 추출
        String email = decodedToken.getEmail();
        if (email == null || email.trim().isEmpty()) {
            // Firebase에서 이메일이 없는 경우도 있을 수 있지만, Google 로그인은 보통 이메일을 포함
            log.warn("Firebase ID Token에 이메일이 없습니다. firebaseUid: {}", firebaseUid);
        }

        // 클라이언트 타입 로깅 (요청에서 제공된 경우)
        if (request.getClientType() != null) {
            log.info("Firebase 로그인 요청 - 클라이언트 타입: {}", request.getClientType());
        } else {
            log.info("Firebase 로그인 요청 - 클라이언트 타입: 미지정");
        }

        // 기존 유저 존재 여부 확인 (Firebase UID로 조회)
        Optional<User> existingUserOpt = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, firebaseUid);
        boolean isNewUser = existingUserOpt.isEmpty();

        User user;
        if (isNewUser) {
            // 새 사용자: name 필수 검증 후 회원가입
            // Firebase Token의 클레임에서 이름 추출 시도
            Map<String, Object> claims = decodedToken.getClaims();
            String name = request.getUserName();
            if (name == null || name.trim().isEmpty()) {
                // 클레임에서 name 추출 시도
                Object nameClaim = claims.get("name");
                if (nameClaim != null) {
                    name = nameClaim.toString();
                }
            }
            
            // Firebase 클레임에서 picture 추출
            String picture = null;
            Object pictureClaim = claims.get("picture");
            if (pictureClaim != null) {
                picture = pictureClaim.toString();
            }

            // 이름 필수 검증 (새 사용자만)
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("AUTH_MISSING_NAME: 사용자 이름이 필요합니다.");
            }
            
            // 이름 길이 검증
            String trimmedName = name.trim();
            if (trimmedName.length() > 10) {
                throw new IllegalArgumentException("사용자 이름은 최대 10자까지 입력 가능합니다.");
            }

            user = User.builder()
                    .email(email)
                    .name(trimmedName)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(firebaseUid)  // Firebase UID 사용
                    .imageUrl(picture)
                    .build();
            user = userRepository.save(user);
            log.info("신규 사용자 회원가입 완료: userId={}, email={}, firebaseUid={}", user.getId(), email, firebaseUid);
        } else {
            // 기존 사용자: name 검증 없이 바로 로그인
            user = existingUserOpt.get();
            
            // 선택적으로 name 업데이트 (request에 제공된 경우)
            String name = request.getUserName();
            boolean updated = false;
            
            if (name != null && !name.trim().isEmpty() && !name.equals(user.getName())) {
                String trimmedName = name.trim();
                // 이름 길이 검증
                if (trimmedName.length() > 10) {
                    throw new IllegalArgumentException("사용자 이름은 최대 10자까지 입력 가능합니다.");
                }
                user.setName(trimmedName);
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
                log.info("사용자 정보 업데이트 완료: userId={}", user.getId());
            }
        }

        // 새 refresh token 발급 및 저장
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
        // Refresh Token 만료 시간은 JWT 토큰의 만료 시간과 동일하게 설정
        long refreshTokenValiditySeconds = jwtTokenProvider.getRefreshTokenValidityMillis() / 1000;
        LocalDateTime refreshExpiry = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(refreshExpiry)
                .build();
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        // 디바이스 토큰 등록 (제공된 경우)
        if (request.getDeviceToken() != null && !request.getDeviceToken().trim().isEmpty()) {
            try {
                com.example.demo.dto.notification.DeviceTokenRequest deviceTokenRequest = 
                    new com.example.demo.dto.notification.DeviceTokenRequest();
                deviceTokenRequest.setDeviceToken(request.getDeviceToken());
                deviceTokenRequest.setDeviceType(request.getDeviceType());
                deviceTokenRequest.setDeviceInfo(request.getDeviceInfo());
                deviceTokenService.registerDeviceToken(user.getId(), deviceTokenRequest);
                log.info("로그인 시 디바이스 토큰 등록 완료: userId={}", user.getId());
            } catch (Exception e) {
                // 디바이스 토큰 등록 실패해도 로그인은 성공 처리
                log.warn("로그인 시 디바이스 토큰 등록 실패: userId={}, error={}", user.getId(), e.getMessage());
            }
        }

        UserResponse userResponse = UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getName())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return AuthTokensResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .isNewUser(isNewUser)
                .user(userResponse)
                .build();
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.
     * Refresh Token 로테이션 정책:
     * 1. Refresh Token은 1회성 토큰이며, 재발급 시 기존 토큰은 즉시 폐기됩니다.
     * 2. 동시 요청이 여러 번 들어오는 경우, 가장 먼저 처리된 요청만 유효합니다.
     * 3. 이후 요청들은 이미 폐기된 Refresh Token을 사용하므로 INVALID_REFRESH_TOKEN을 반환합니다.
     * 
     * @param refreshTokenValue 리프레시 토큰 문자열
     * @return 새로운 Access Token, 새로운 Refresh Token, 사용자 정보
     * @throws IllegalArgumentException 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public AuthTokensResponse refreshAccessToken(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 리프레시 토큰이 없습니다.");
        }

        try {
            // 1. JWT 토큰 검증 (서명, 만료 시간 등)
            String tokenType = jwtTokenProvider.getTokenType(refreshTokenValue);
            if (!"REFRESH".equals(tokenType)) {
                throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 리프레시 토큰이 아닙니다.");
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

            // 2. DB에서 Refresh Token 조회 및 검증 (Pessimistic Lock으로 동시 요청 방지)
            // 가장 먼저 도착한 요청만 락을 획득하고, 나머지는 대기 후 이미 삭제된 토큰을 조회하게 됨
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenWithLock(refreshTokenValue);
            if (refreshTokenOpt.isEmpty()) {
                log.warn("리프레시 토큰이 DB에 존재하지 않음 (이미 폐기되었거나 유효하지 않음): userId={}", userId);
                throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 유효하지 않은 리프레시 토큰입니다.");
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            
            // 3. DB에 저장된 만료 시간 확인
            if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("리프레시 토큰이 만료됨: userId={}, expiresAt={}", userId, refreshToken.getExpiresAt());
                // 만료된 토큰은 DB에서 삭제
                refreshTokenRepository.delete(refreshToken);
                throw new IllegalArgumentException("AUTH_TOKEN_EXPIRED: 리프레시 토큰이 만료되었습니다.");
            }

            // 4. 사용자 정보 조회
            User user = refreshToken.getUser();
            if (user == null) {
                throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 사용자 정보를 찾을 수 없습니다.");
            }

            // 5. Refresh Token 로테이션: 기존 토큰 삭제 및 새 토큰 생성
            // 락이 걸린 상태에서 삭제하므로 동시 요청 중 첫 번째 요청만 성공
            refreshTokenRepository.delete(refreshToken);
            log.debug("기존 리프레시 토큰 삭제 완료: userId={}", userId);

            // 6. 새로운 Refresh Token 생성 및 저장
            String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
            long refreshTokenValiditySeconds = jwtTokenProvider.getRefreshTokenValidityMillis() / 1000;
            LocalDateTime refreshExpiry = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);

            RefreshToken newRefreshToken = RefreshToken.builder()
                    .user(user)
                    .token(newRefreshTokenValue)
                    .expiresAt(refreshExpiry)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
            log.debug("새 리프레시 토큰 생성 완료: userId={}", userId);

            // 7. 새로운 Access Token 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());

            // 8. 응답 생성 (새로운 Access Token과 Refresh Token만 반환, 사용자 정보 제외)
            return AuthTokensResponse.builder()
                    .userId(user.getId())
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshTokenValue) // 새로운 Refresh Token 반환 (로테이션)
                    .isNewUser(false) // 재발급이므로 신규 사용자 아님
                    .user(null) // 토큰 재발급 시 사용자 정보는 제외
                    .build();

        } catch (ExpiredJwtException e) {
            log.warn("만료된 리프레시 토큰으로 재발급 시도: {}", e.getMessage());
            throw new IllegalArgumentException("AUTH_TOKEN_EXPIRED: 리프레시 토큰이 만료되었습니다.");
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("유효하지 않은 리프레시 토큰: {}", e.getMessage());
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 유효하지 않은 리프레시 토큰입니다.");
        } catch (Exception e) {
            log.error("토큰 재발급 중 오류 발생", e);
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN: 토큰 재발급에 실패했습니다.");
        }
    }

    /**
     * 로그아웃: Refresh Token과 Device Token 삭제
     * 
     * @param userId 사용자 ID
     * @param refreshToken 삭제할 Refresh Token (선택적, 제공되지 않으면 사용자의 모든 Refresh Token 삭제)
     */
    @Transactional
    public void logout(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // Refresh Token 삭제
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            // 특정 Refresh Token만 삭제
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(rt -> {
                        if (rt.getUser().getId().equals(userId)) {
                            refreshTokenRepository.delete(rt);
                            log.info("로그아웃: Refresh Token 삭제 완료 - userId={}, token={}", userId, refreshToken);
                        } else {
                            log.warn("로그아웃: Refresh Token 소유자 불일치 - userId={}, tokenUserId={}", userId, rt.getUser().getId());
                        }
                    });
        } else {
            // 사용자의 모든 Refresh Token 삭제
            refreshTokenRepository.deleteByUser(user);
            log.info("로그아웃: 모든 Refresh Token 삭제 완료 - userId={}", userId);
        }
        
        // Device Token 삭제
        deviceTokenService.deleteDeviceTokenByUserId(userId);
        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 회원탈퇴: 사용자 삭제
     * 팀장인 경우 회원탈퇴 불가 (먼저 팀을 삭제해야 함)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 팀장인지 확인 (팀장은 회원탈퇴 불가)
        List<TeamDetailResponse> ownedTeams = teamRepository.findByOwnerId(userId);
        if (!ownedTeams.isEmpty()) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장은 회원탈퇴할 수 없습니다. 먼저 소유한 팀을 삭제해주세요.");
        }
        
        // Refresh Token 삭제
        refreshTokenRepository.deleteByUser(user);
        
        // Device Token 삭제
        deviceTokenService.deleteDeviceTokenByUserId(userId);
        
        // 사용자 삭제 (CASCADE로 관련 데이터 자동 삭제)
        userRepository.delete(user);
        log.info("회원탈퇴 완료: userId={}", userId);
    }
}
