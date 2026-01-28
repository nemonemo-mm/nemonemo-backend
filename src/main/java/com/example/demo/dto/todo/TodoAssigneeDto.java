package com.example.demo.dto.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoAssigneeDto {

    @Schema(description = "담당자 팀멤버 ID", example = "2")
    private Long memberId;

    @Schema(description = "담당자 사용자 이름", example = "홍길동")
    private String userName;
}























