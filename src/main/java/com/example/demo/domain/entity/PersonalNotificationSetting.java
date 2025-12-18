package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "personal_notification_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_personal_notification_setting_user",
                columnNames = {"user_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalNotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "enable_all_personal_notifications", nullable = false)
    @Builder.Default
    private Boolean enableAllPersonalNotifications = false;

    @Column(name = "enable_schedule_change_notification", nullable = false)
    @Builder.Default
    private Boolean enableScheduleChangeNotification = true;

    @Column(name = "enable_schedule_pre_notification", nullable = false)
    @Builder.Default
    private Boolean enableSchedulePreNotification = false;

    @Column(name = "schedule_pre_notification_minutes", columnDefinition = "INTEGER[]")
    private Integer[] schedulePreNotificationMinutes;  // 10, 30, 60 (분)

    @Column(name = "enable_todo_change_notification", nullable = false)
    @Builder.Default
    private Boolean enableTodoChangeNotification = true;

    @Column(name = "enable_todo_deadline_notification", nullable = false)
    @Builder.Default
    private Boolean enableTodoDeadlineNotification = false;

    @Column(name = "todo_deadline_notification_minutes", columnDefinition = "INTEGER[]")
    private Integer[] todoDeadlineNotificationMinutes;  // 10, 30, 60 (분)

    @Column(name = "enable_notice_notification", nullable = false)
    @Builder.Default
    private Boolean enableNoticeNotification = true;

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

