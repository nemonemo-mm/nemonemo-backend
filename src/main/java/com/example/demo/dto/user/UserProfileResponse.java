package com.example.demo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 프로필 정보")
public interface UserProfileResponse {
    @Schema(description = "사용자 ID", example = "1")
    Long getUserId();

    @Schema(description = "사용자 이름", example = "홍길동")
    String getUserName();

    @Schema(description = "사용자 이메일", example = "user@example.com")
    String getUserEmail();

    @Schema(description = "프로필 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...")
    String getUserImageUrl();
}

