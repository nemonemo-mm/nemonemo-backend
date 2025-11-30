package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private String title;
    private String description;
    private TodoStatus status;
    private LocalDateTime dueAt;
    private Long createdById;
    private String createdByName;
    private Long assigneeMemberId;
    private String assigneeMemberNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





