package com.example.demo.repository;

import com.example.demo.domain.entity.PersonalNotificationSetting;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notification.PersonalNotificationSettingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonalNotificationSettingRepository extends JpaRepository<PersonalNotificationSetting, Long> {
    Optional<PersonalNotificationSetting> findByUser(User user);
    
    Optional<PersonalNotificationSetting> findByUserId(Long userId);
    
    @Query("""
            select 
                pns.id as id,
                pns.user.id as userId,
                pns.enableAllPersonalNotifications as enableAllPersonalNotifications,
                pns.enableScheduleChangeNotification as enableScheduleChangeNotification,
                pns.enableSchedulePreNotification as enableSchedulePreNotification,
                pns.schedulePreNotificationMinutes as schedulePreNotificationMinutes,
                pns.enableTodoChangeNotification as enableTodoChangeNotification,
                pns.enableTodoDeadlineNotification as enableTodoDeadlineNotification,
                pns.todoDeadlineNotificationMinutes as todoDeadlineNotificationMinutes,
                pns.enableNoticeNotification as enableNoticeNotification,
                pns.createdAt as createdAt,
                pns.updatedAt as updatedAt
            from PersonalNotificationSetting pns
            where pns.user.id = :userId
            """)
    Optional<PersonalNotificationSettingResponse> findResponseByUserId(@Param("userId") Long userId);
}


