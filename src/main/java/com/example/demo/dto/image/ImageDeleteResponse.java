package com.example.demo.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이미지 삭제 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDeleteResponse {
    @Schema(description = "사용자 ID (프로필 이미지 삭제인 경우)", example = "1")
    private Long userId;

    @Schema(description = "팀 ID (팀 이미지 삭제인 경우)", example = "1")
    private Long teamId;
}
