package com.example.demo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이미지 업로드 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {
    @Schema(description = "사용자 ID (프로필 이미지인 경우)", example = "1")
    private Long userId;

    @Schema(description = "팀 ID (팀 이미지인 경우)", example = "1")
    private Long teamId;

    @Schema(description = "업로드된 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...")
    private String imageUrl;
}
