package com.example.demo.auth.dto;

import com.example.demo.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "소셜 로그인 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokensResponse {
    @Schema(description = "액세스 토큰 (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰 (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "신규 사용자 여부", example = "true")
    private boolean isNewUser;

    @Schema(description = "사용자 정보")
    private UserResponse user;
}
