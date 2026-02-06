package com.example.demo.controller;

import com.example.demo.dto.alert.AlertResponseDto;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.AlertService;
import com.example.demo.service.AlertService.AlertScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alert API", description = "알림함(전체/개인, 팀별) 조회용 API")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final JwtAuthenticationHelper jwtAuthenticationHelper;

    @Operation(
            summary = "알림 목록 조회",
            description = "알림함에서 전체/개인 알림을 조회합니다. 기본값은 전체입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AlertResponseDto.class))))
            }
    )
    @GetMapping
    public ResponseEntity<List<AlertResponseDto>> getAlerts(
            @RequestParam(name = "scope", defaultValue = "ALL") AlertScope scope
    ) {
        Long userId = jwtAuthenticationHelper.getCurrentUserId();
        List<AlertResponseDto> alerts = alertService.getAlerts(userId, scope);
        return ResponseEntity.ok(alerts);
    }

    @Operation(
            summary = "팀별 알림 목록 조회",
            description = "특정 팀에 대한 알림만 조회합니다. (해당 팀의 멤버만 조회 가능)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팀별 알림 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AlertResponseDto.class))))
            }
    )
    @GetMapping("/team")
    public ResponseEntity<List<AlertResponseDto>> getTeamAlerts(
            @RequestParam(name = "teamId") Long teamId
    ) {
        Long userId = jwtAuthenticationHelper.getCurrentUserId();
        List<AlertResponseDto> alerts = alertService.getTeamAlerts(userId, teamId);
        return ResponseEntity.ok(alerts);
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "단일 알림을 읽음 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 읽음 처리 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AlertResponseDto.class)))
            }
    )
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<AlertResponseDto> markAlertAsRead(
            @PathVariable("alertId") Long alertId
    ) {
        Long userId = jwtAuthenticationHelper.getCurrentUserId();
        AlertResponseDto response = alertService.markAsRead(userId, alertId);
        return ResponseEntity.ok(response);
    }
}


