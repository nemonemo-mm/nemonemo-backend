package com.example.demo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "디바이스 토큰 조회 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenResponse {
    @Schema(description = "디바이스 토큰 (Expo Push Token)", example = "ExponentPushToken[xxxxxxxxxxxxx]")
    private String deviceToken;

    @Schema(description = "디바이스 타입 (iOS, Android)")
    private String deviceType;

    @Schema(description = "디바이스 정보")
    private String deviceInfo;

    @Schema(description = "등록일시")
    private LocalDateTime registeredAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}
