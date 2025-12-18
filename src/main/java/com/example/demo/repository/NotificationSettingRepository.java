package com.example.demo.repository;

import com.example.demo.domain.entity.NotificationSetting;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByUserAndTeam(User user, Team team);
    
    Optional<NotificationSetting> findByUserIdAndTeamId(Long userId, Long teamId);
    
    List<NotificationSetting> findByUser(User user);
    
    List<NotificationSetting> findByUserId(Long userId);
    
    List<NotificationSetting> findByTeam(Team team);
    
    List<NotificationSetting> findByTeamId(Long teamId);
}


