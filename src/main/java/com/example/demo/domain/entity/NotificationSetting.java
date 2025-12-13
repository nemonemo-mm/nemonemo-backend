package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notification_setting_user_team",
                columnNames = {"user_id", "team_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "enable_team_alarm", nullable = false)
    @Builder.Default
    private Boolean enableTeamAlarm = true;

    @Column(name = "enable_schedule_start_alarm", nullable = false)
    @Builder.Default
    private Boolean enableScheduleStartAlarm = true;

    @Column(name = "schedule_start_before_minutes")
    private Integer scheduleStartBeforeMinutes;  // 10, 30, 60 (분)

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}












