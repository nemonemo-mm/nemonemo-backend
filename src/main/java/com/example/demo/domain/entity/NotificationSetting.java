package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notification_setting_user_team",
                columnNames = {"user_id", "team_id"}
        ))
@Getter
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

    @Column(name = "enable_due_alarm", nullable = false)
    @Builder.Default
    private Boolean enableDueAlarm = true;

    @Column(name = "due_alarm_before_min", nullable = false)
    @Builder.Default
    private Integer dueAlarmBeforeMin = 30;

    @Column(name = "enable_pinned_alarm", nullable = false)
    @Builder.Default
    private Boolean enablePinnedAlarm = true;

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





