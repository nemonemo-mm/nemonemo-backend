package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todo_attendee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoAttendee {

    @EmbeddedId
    private TodoAttendeeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("todoId")
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private TeamMember member;
}



































