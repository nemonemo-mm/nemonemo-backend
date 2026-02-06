package com.example.demo.dto.alert;

import com.example.demo.domain.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "알림(알림함) 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponseDto {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 타입", example = "SCHEDULE_ASSIGNEE_ADDED")
    private AlertType type;

    @Schema(description = "팀 ID (팀 관련 알림인 경우)", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름 (팀 관련 알림인 경우)", example = "NemoNemo")
    private String teamName;

    @Schema(description = "알림 내용", example = "홍길동님에게 새로운 스케줄이 등록되었습니다.")
    private String content;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime time;

    @Schema(description = "읽은 시각 (읽지 않은 경우 null)", example = "2024-01-16T09:00:00.000Z")
    private LocalDateTime readAt;
}


