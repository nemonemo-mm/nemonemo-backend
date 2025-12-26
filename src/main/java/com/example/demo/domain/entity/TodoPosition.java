package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todo_position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoPosition {

    @EmbeddedId
    private TodoPositionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("todoId")
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("positionId")
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}











