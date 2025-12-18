package com.example.demo.repository;

import com.example.demo.domain.entity.NotificationSetting;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notification.TeamNotificationSettingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByUserAndTeam(User user, Team team);
    
    Optional<NotificationSetting> findByUserIdAndTeamId(Long userId, Long teamId);
    
    List<NotificationSetting> findByUser(User user);
    
    List<NotificationSetting> findByUserId(Long userId);
    
    List<NotificationSetting> findByTeam(Team team);
    
    List<NotificationSetting> findByTeamId(Long teamId);
    
    @Query("""
            select 
                ns.id as id,
                ns.user.id as userId,
                ns.team.id as teamId,
                ns.team.name as teamName,
                ns.enableTeamAlarm as enableTeamAlarm,
                ns.enableScheduleChangeNotification as enableScheduleChangeNotification,
                ns.enableSchedulePreNotification as enableSchedulePreNotification,
                ns.schedulePreNotificationMinutes as schedulePreNotificationMinutes,
                ns.enableTodoChangeNotification as enableTodoChangeNotification,
                ns.enableTodoDeadlineNotification as enableTodoDeadlineNotification,
                ns.todoDeadlineNotificationMinutes as todoDeadlineNotificationMinutes,
                ns.enableTeamMemberNotification as enableTeamMemberNotification,
                ns.createdAt as createdAt,
                ns.updatedAt as updatedAt
            from NotificationSetting ns
            where ns.user.id = :userId and ns.team.id = :teamId
            """)
    Optional<TeamNotificationSettingResponse> findResponseByUserIdAndTeamId(
            @Param("userId") Long userId,
            @Param("teamId") Long teamId
    );
}


