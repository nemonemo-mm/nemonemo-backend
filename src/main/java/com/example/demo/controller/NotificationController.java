package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.notification.DeviceTokenRequest;
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("개인 알림 설정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "개인 알림 설정 수정", description = "현재 사용자의 개인 알림 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonalNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 알림 설정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 알림 설정 수정", description = "특정 팀의 알림 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamNotificationSettingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 알림 설정 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 디바이스 토큰 관리 ==========

    @Operation(summary = "디바이스 토큰 등록", description = "FCM 디바이스 토큰을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
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
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("디바이스 토큰 등록 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ========== 헬퍼 메서드 ==========


    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        ErrorResponse error = new ErrorResponse("UNAUTHORIZED", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", message);
        return ResponseEntity.status(status).body(error);
    }
}


