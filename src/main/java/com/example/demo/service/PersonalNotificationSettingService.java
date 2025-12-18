package com.example.demo.service;

import com.example.demo.domain.entity.PersonalNotificationSetting;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notification.PersonalNotificationSettingRequest;
import com.example.demo.dto.notification.PersonalNotificationSettingResponse;
import com.example.demo.repository.PersonalNotificationSettingRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalNotificationSettingService {

    private final PersonalNotificationSettingRepository personalNotificationSettingRepository;
    private final UserRepository userRepository;

    /**
     * 개인 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public PersonalNotificationSettingResponse getPersonalNotificationSetting(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return personalNotificationSettingRepository.findResponseByUserId(userId)
                .orElseGet(() -> {
                    // 기본값으로 생성
                    User user = userRepository.findById(userId).orElseThrow();
                    PersonalNotificationSetting defaultSetting = PersonalNotificationSetting.builder()
                            .user(user)
                            .build();
                    personalNotificationSettingRepository.save(defaultSetting);
                    // 저장 후 다시 조회하여 인터페이스 프로젝션으로 반환
                    return personalNotificationSettingRepository.findResponseByUserId(userId)
                            .orElseThrow(() -> new IllegalStateException("알림 설정 저장 후 조회 실패"));
                });
    }

    /**
     * 개인 알림 설정 수정
     */
    @Transactional
    public PersonalNotificationSettingResponse updatePersonalNotificationSetting(Long userId, PersonalNotificationSettingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        PersonalNotificationSetting setting = personalNotificationSettingRepository.findByUser(user)
                .orElseGet(() -> PersonalNotificationSetting.builder()
                        .user(user)
                        .build());

        // 부분 업데이트
        if (request.getEnableAllPersonalNotifications() != null) {
            setting.setEnableAllPersonalNotifications(request.getEnableAllPersonalNotifications());
        }
        if (request.getEnableScheduleChangeNotification() != null) {
            setting.setEnableScheduleChangeNotification(request.getEnableScheduleChangeNotification());
        }
        if (request.getEnableSchedulePreNotification() != null) {
            setting.setEnableSchedulePreNotification(request.getEnableSchedulePreNotification());
        }
        if (request.getSchedulePreNotificationMinutes() != null) {
            setting.setSchedulePreNotificationMinutes(request.getSchedulePreNotificationMinutes());
        }
        if (request.getEnableTodoChangeNotification() != null) {
            setting.setEnableTodoChangeNotification(request.getEnableTodoChangeNotification());
        }
        if (request.getEnableTodoDeadlineNotification() != null) {
            setting.setEnableTodoDeadlineNotification(request.getEnableTodoDeadlineNotification());
        }
        if (request.getTodoDeadlineNotificationMinutes() != null) {
            setting.setTodoDeadlineNotificationMinutes(request.getTodoDeadlineNotificationMinutes());
        }
        if (request.getEnableNoticeNotification() != null) {
            setting.setEnableNoticeNotification(request.getEnableNoticeNotification());
        }

        setting = personalNotificationSettingRepository.save(setting);
        return personalNotificationSettingRepository.findResponseByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("알림 설정 저장 후 조회 실패"));
    }
}


