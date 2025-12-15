package com.example.demo.dto.user;

import com.example.demo.domain.enums.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "사용자 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "인증 제공자", example = "GOOGLE")
    private AuthProvider provider;

    @Schema(description = "소셜 제공자 ID", example = "12345678901234567890")
    private String providerId;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}












