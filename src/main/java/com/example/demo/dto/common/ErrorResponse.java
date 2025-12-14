package com.example.demo.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "에러 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    @Schema(description = "에러 코드 (예: VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, TEAM_NOT_FOUND, INTERNAL_SERVER_ERROR 등)", example = "UNAUTHORIZED")
    private String code;

    @Schema(description = "에러 메시지", example = "인증이 필요합니다.")
    private String message;
}
