package com.example.demo.repository;

import com.example.demo.domain.entity.PersonalNotificationSetting;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalNotificationSettingRepository extends JpaRepository<PersonalNotificationSetting, Long> {
    Optional<PersonalNotificationSetting> findByUser(User user);
    
    Optional<PersonalNotificationSetting> findByUserId(Long userId);
}


