package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todo_schedule_link")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TodoScheduleLinkId.class)
public class TodoScheduleLink {
    @Id
    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Id
    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", insertable = false, updatable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;
}

