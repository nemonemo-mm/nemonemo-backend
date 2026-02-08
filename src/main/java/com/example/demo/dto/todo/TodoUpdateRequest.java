package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TodoUpdateRequest {

    @Schema(description = "투두 제목", example = "프로젝트 계획 수립(수정)")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    private String title;

    @Schema(description = "투두 설명", example = "프로젝트 초기 계획 수정")
    private String description;

    private TodoStatus status;

    private LocalDateTime endAt;

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    @Schema(description = "장소", example = "회의실 A")
    private String place;

    @Size(max = 1000, message = "URL은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "URL", example = "https://example.com")
    private String url;

    private List<Long> assigneeMemberIds;

    private List<Long> positionIds;
}


