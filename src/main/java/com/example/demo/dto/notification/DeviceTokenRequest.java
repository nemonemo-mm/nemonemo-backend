package com.example.demo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "디바이스 토큰 등록 요청")
@Getter
@Setter
public class DeviceTokenRequest {
    @NotBlank(message = "디바이스 토큰은 필수입니다")
    @Schema(description = "Expo Push Token (ExponentPushToken[...] 형식)", example = "ExponentPushToken[xxxxxxxxxxxxx]")
    private String deviceToken;
}

