package com.example.demo.domain.entity;

import com.example.demo.domain.enums.TodoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "todo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 투두 상태 (TODO, DONE)
     * PostgreSQL ENUM 타입(todo_status)과 매핑
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "todo_status")
    @Builder.Default
    private TodoStatus status = TodoStatus.TODO;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;  // 종료 시간

    @Column(length = 100)
    private String place;

    @Column(name = "url", length = 1000)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoAttendee> assignees;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoPosition> positions;

    // 반복 일정 관련 필드 (스케줄과 동일한 구조)
    @Column(name = "repeat_type", length = 20)
    private String repeatType;  // NONE, DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "repeat_interval")
    @Builder.Default
    private Integer repeatInterval = 1;  // 반복 간격

    @Column(name = "repeat_days", columnDefinition = "INTEGER[]")
    private Integer[] repeatDays;  // 주간 반복 시 요일 배열 (0=일요일, 1=월요일, ...)

    @Column(name = "repeat_month_day")
    private Integer repeatMonthDay;  // 월간/연간 반복 시 날짜 (1-31)

    @Column(name = "repeat_end_date")
    private LocalDateTime repeatEndDate;  // 반복 종료일

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







