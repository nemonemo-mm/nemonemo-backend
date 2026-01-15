package com.example.demo.dto.auth;

import com.example.demo.domain.enums.AuthProvider;
import com.example.demo.domain.enums.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "소셜 로그인 요청")
public class SocialLoginRequest {

    @NotNull
    @Schema(description = "소셜 로그인 제공자", example = "GOOGLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private AuthProvider provider;

    @NotBlank
    @Schema(description = "Firebase Authentication SDK에서 발급받은 ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firebaseIdToken;

    @Schema(description = "클라이언트 플랫폼 타입 (미제공 시 ID 토큰에서 자동 감지)", 
            example = "WEB", 
            allowableValues = {"IOS", "ANDROID", "WEB"})
    private ClientType clientType;

    @Schema(description = "사용자 이름. null 또는 생략 시 신규/기존 사용자 여부만 확인, 값 제공 시 회원가입 또는 로그인 완료", 
            example = "홍길동 | null")
    private String userName;

    @Schema(description = "Expo Push Token (선택, 푸시 알림 수신 시 필요)", example = "ExponentPushToken[xxxxxxxxxxxxx]")
    private String deviceToken;
}
