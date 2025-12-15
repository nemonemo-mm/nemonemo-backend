package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String place;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "reminder_offset_minutes")
    private Integer reminderOffsetMinutes;

    @Column(name = "is_all_day", nullable = false)
    @Builder.Default
    private Boolean isAllDay = false;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "url", length = 1000)
    private String url;

    // 반복 일정 관련 필드
    @Column(name = "repeat_type", length = 20)
    private String repeatType;  // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "repeat_interval")
    @Builder.Default
    private Integer repeatInterval = 1;  // 반복 간격 (n일, n주, n개월, n년)

    @Column(name = "repeat_days", columnDefinition = "INTEGER[]")
    private Integer[] repeatDays;  // 매주 반복 시 요일 배열 (0=일요일, 1=월요일, ...)

    @Column(name = "repeat_month_day")
    private Integer repeatMonthDay;  // 매월 반복 시 날짜 (1-31)

    @Column(name = "repeat_week_ordinal")
    private Integer repeatWeekOrdinal;  // 매월 n째주 (1=첫째주, 2=둘째주, ...)

    @Column(name = "repeat_week_day")
    private Integer repeatWeekDay;  // 매월 n째주 요일 (0=일요일, 1=월요일, ...)

    @Column(name = "repeat_end_date")
    private LocalDateTime repeatEndDate;  // 반복 종료일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_schedule_id")
    private Schedule parentSchedule;  // 반복 일정의 부모 일정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

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












