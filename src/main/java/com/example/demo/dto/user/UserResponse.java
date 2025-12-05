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
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "인증 제공자", example = "GOOGLE")
    private AuthProvider provider;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String imageUrl;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}












