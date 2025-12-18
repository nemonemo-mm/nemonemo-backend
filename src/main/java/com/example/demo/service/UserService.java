package com.example.demo.service;

import com.example.demo.domain.entity.User;
import com.example.demo.dto.user.UserProfileResponse;
import com.example.demo.dto.user.UserProfileUpdateRequest;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        return userRepository.findProfileResponseById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자 프로필 수정 (이름만 수정 가능)
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이름 수정
        if (request.getUserName() != null) {
            String trimmedName = request.getUserName().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("사용자 이름은 필수입니다.");
            }
            if (trimmedName.length() > 10) {
                throw new IllegalArgumentException("사용자 이름은 최대 10자까지 입력 가능합니다.");
            }
            user.setName(trimmedName);
        }

        userRepository.save(user);

        // 수정 후 인터페이스 프로젝션으로 반환
        return userRepository.findProfileResponseById(userId)
                .orElseThrow(() -> new IllegalStateException("프로필 수정 후 조회 실패"));
    }
}

