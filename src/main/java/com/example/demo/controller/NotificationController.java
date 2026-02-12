package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.notification.DeviceTokenRequest;
import com.example.demo.dto.notification.DeviceTokenResponse;
import com.example.demo.dto.notification.PersonalNotificationSettingRequest;
import com.example.demo.dto.notification.PersonalNotificationSettingResponse;
import com.example.demo.dto.notification.TeamNotificationSettingRequest;
import com.example.demo.dto.notification.TeamNotificationSettingResponse;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.DeviceTokenService;
import com.example.demo.service.PersonalNotificationSettingService;
import com.example.demo.service.TeamNotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 설정", description = "개인 알림 설정, 팀 알림 설정, 디바이스 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PersonalNotificationSettingService personalNotificationSettingService;
    private final TeamNotificationSettingService teamNotificationSettingService;
    private final DeviceTokenService deviceTokenService;
    private final JwtAuthenticationHelper jwtHelper;

    // ========== 개인 알림 설정 ==========

    @Operation(summary = "개인 알림 설정 조회", description = "현재 사용자의 개인 알림 설정을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonalNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"개인 알림 설정 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/personal")
    public ResponseEntity<?> getPersonalNotificationSetting(
) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            PersonalNotificationSettingResponse response = personalNotificationSettingService.getPersonalNotificationSetting(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("개인 알림 설정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "개인 알림 설정 수정", description = "현재 사용자의 개인 알림 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonalNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"개인 알림 설정 수정 중 오류가 발생했습니다.\"}")))
    })
    @PutMapping("/personal")
    public ResponseEntity<?> updatePersonalNotificationSetting(
            @Valid @RequestBody PersonalNotificationSettingRequest request) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            PersonalNotificationSettingResponse response = personalNotificationSettingService.updatePersonalNotificationSetting(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("개인 알림 설정 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 팀 알림 설정 ==========

    @Operation(summary = "팀 알림 설정 조회", description = "특정 팀의 알림 설정을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 팀 알림 설정을 조회할 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 알림 설정 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<?> getTeamNotificationSetting(
            @PathVariable Long teamId) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            TeamNotificationSettingResponse response = teamNotificationSettingService.getTeamNotificationSetting(userId, teamId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("팀 알림 설정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 알림 설정 수정", description = "특정 팀의 알림 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 수정 가능)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 팀 알림 설정을 수정할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 알림 설정 수정 중 오류가 발생했습니다.\"}")))
    })
    @PutMapping("/teams/{teamId}")
    public ResponseEntity<?> updateTeamNotificationSetting(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamNotificationSettingRequest request) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            TeamNotificationSettingResponse response = teamNotificationSettingService.updateTeamNotificationSetting(userId, teamId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("팀 알림 설정 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 디바이스 토큰 관리 ==========

    @Operation(summary = "디바이스 토큰 조회", description = "현재 사용자의 등록된 디바이스 토큰을 조회합니다. 등록된 토큰이 없으면 deviceToken이 null입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceTokenResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"디바이스 토큰 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/device-token")
    public ResponseEntity<?> getDeviceToken() {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            DeviceTokenResponse response = deviceTokenService.getDeviceTokenResponseByUserId(userId)
                    .orElse(DeviceTokenResponse.builder()
                            .deviceToken(null)
                            .deviceType(null)
                            .deviceInfo(null)
                            .registeredAt(null)
                            .updatedAt(null)
                            .build());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("디바이스 토큰 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "디바이스 토큰 등록", description = "Expo Push Token을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 등)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"디바이스 토큰은 필수입니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"디바이스 토큰 등록 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/device-token")
    public ResponseEntity<?> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequest request) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            deviceTokenService.registerDeviceToken(userId, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createErrorResponse("디바이스 토큰 등록 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ========== 헬퍼 메서드 ==========


    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        ErrorResponse error = new ErrorResponse("UNAUTHORIZED", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", message);
        return ResponseEntity.status(status).body(error);
    }
}


