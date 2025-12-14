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
    @Schema(description = "업로드된 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...")
    private String imageUrl;
}
