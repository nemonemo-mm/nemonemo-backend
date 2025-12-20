package com.example.demo.service;

import com.example.demo.domain.entity.DeviceToken;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notification.DeviceTokenRequest;
import com.example.demo.repository.DeviceTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    /**
     * 디바이스 토큰 등록 (단일 디바이스 지원: 사용자당 1개만 유지)
     */
    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자의 기존 토큰 확인
        Optional<DeviceToken> existingTokenOpt = deviceTokenRepository.findByUserId(userId);
        
        if (existingTokenOpt.isPresent()) {
            // 기존 토큰이 있으면 덮어쓰기 (토큰 값과 정보 업데이트)
            DeviceToken existingToken = existingTokenOpt.get();
            existingToken.setDeviceToken(request.getDeviceToken());
            if (request.getDeviceType() != null) {
                existingToken.setDeviceType(request.getDeviceType());
            }
            deviceTokenRepository.save(existingToken);
            log.info("디바이스 토큰 업데이트: token={}, userId={}", request.getDeviceToken(), userId);
        } else {
            // 기존 토큰이 없으면 새로 생성
            DeviceToken deviceToken = DeviceToken.builder()
                    .user(user)
                    .deviceToken(request.getDeviceToken())
                    .deviceType(request.getDeviceType())
                    .build();
            deviceTokenRepository.save(deviceToken);
            log.info("새 디바이스 토큰 등록: token={}, userId={}", request.getDeviceToken(), userId);
        }
    }

    /**
     * 사용자의 디바이스 토큰 조회 (단일 디바이스 지원)
     */
    @Transactional(readOnly = true)
    public Optional<String> getDeviceTokenByUserId(Long userId) {
        return deviceTokenRepository.findByUserId(userId)
                .map(DeviceToken::getDeviceToken);
    }

    /**
     * 사용자의 모든 디바이스 토큰 삭제 (로그아웃/회원탈퇴 시 사용)
     */
    @Transactional
    public void deleteDeviceTokenByUserId(Long userId) {
        deviceTokenRepository.findByUserId(userId)
                .ifPresent(deviceToken -> {
                    deviceTokenRepository.delete(deviceToken);
                    log.info("사용자 디바이스 토큰 삭제: userId={}", userId);
                });
    }
}


