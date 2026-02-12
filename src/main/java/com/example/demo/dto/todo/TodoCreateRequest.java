package com.example.demo.dto.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TodoCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 30, message = "제목은 최대 30자까지 입력 가능합니다.")
    @Schema(description = "투두 제목 (필수, 최대 30자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 계획 수립")
    private String title;

    @Schema(description = "투두 설명 (선택, TEXT 타입, 길이 제한 없음)", example = "프로젝트 초기 계획을 수립합니다")
    private String description;

    @NotNull
    @Schema(description = "종료일시 (필수)", example = "2024-01-15T11:30:00.000Z")
    private LocalDateTime endAt;

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    @Schema(description = "장소 (선택, 최대 100자)", example = "회의실 A")
    private String place;

    @Size(max = 1000, message = "URL은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "URL (선택, 최대 1000자)", example = "https://example.com")
    private String url;

    // 담당자 목록 (선택)
    private List<Long> assigneeMemberIds;

    // 포지션 목록
    private List<Long> positionIds;
}












