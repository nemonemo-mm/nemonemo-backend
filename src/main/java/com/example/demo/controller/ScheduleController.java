package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.schedule.ScheduleCreateRequest;
import com.example.demo.dto.schedule.ScheduleResponse;
import com.example.demo.dto.schedule.ScheduleUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.ScheduleService.RepeatScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "스케줄", description = "팀 스케줄 생성/수정/삭제 및 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "스케줄 생성", description = "팀 단위 스케줄을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 잘못된 반복 규칙 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validation", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"제목은 필수입니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 일정을 생성할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"스케줄 생성 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/schedules")
    public ResponseEntity<?> createSchedule(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            ScheduleResponse response = scheduleService.createSchedule(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("스케줄 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "스케줄 수정", description = "스케줄을 수정합니다. 반복 스케줄의 경우 scope 파라미터로 적용 범위를 선택할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 잘못된 scope 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"유효하지 않은 요청입니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 일정을 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"SCHEDULE_NOT_FOUND\",\"message\":\"일정을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"스케줄 수정 중 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/schedules/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "스케줄 ID", example = "1")
            @PathVariable Long scheduleId,
            @Parameter(description = "반복 스케줄 수정 범위 (기본값: ALL)", example = "ALL")
            @RequestParam(name = "scope", required = false, defaultValue = "ALL") RepeatScope scope,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            ScheduleResponse response = scheduleService.updateSchedule(userId, scheduleId, request, scope);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("스케줄 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다. 반복 스케줄의 경우 scope 파라미터로 적용 범위를 선택할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 일정을 삭제할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"SCHEDULE_NOT_FOUND\",\"message\":\"일정을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"스케줄 삭제 중 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "스케줄 ID", example = "1")
            @PathVariable Long scheduleId,
            @Parameter(description = "반복 스케줄 삭제 범위 (기본값: ALL)", example = "THIS_ONLY")
            @RequestParam(name = "scope", required = false, defaultValue = "ALL") RepeatScope scope
    ) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            scheduleService.deleteSchedule(userId, scheduleId, scope);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("스케줄 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 스케줄 조회", description = "특정 팀의 기간 내 스케줄 목록을 조회합니다. positionIds 파라미터로 포지션별 필터링이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 팀 일정을 조회할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 일정 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/teams/{teamId}/schedules")
    public ResponseEntity<?> getTeamSchedules(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "조회 시작일시 (ISO 8601)", example = "2025-01-01T00:00:00Z")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "조회 종료일시 (ISO 8601)", example = "2025-01-31T23:59:59Z")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(description = "포지션 ID 목록 (선택, 포지션별 필터링)", example = "[1, 2, 3]")
            @RequestParam(value = "positionIds", required = false) List<Long> positionIds
    ) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<ScheduleResponse> responses = scheduleService.getTeamSchedules(userId, teamId, start, end, positionIds);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 일정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "내 스케줄 조회", description = "개인 화면용으로, 내가 참석자로 포함된 스케줄만 기간 내 조회합니다. teamId로 특정 팀 필터링, positionIds로 포지션별 필터링이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"내 일정 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/me/schedules")
    public ResponseEntity<?> getMySchedules(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "조회 시작일시 (ISO 8601)", example = "2025-01-01T00:00:00Z")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "조회 종료일시 (ISO 8601)", example = "2025-01-31T23:59:59Z")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(description = "팀 ID (선택, 특정 팀으로 필터링)", example = "1")
            @RequestParam(value = "teamId", required = false) Long teamId,
            @Parameter(description = "포지션 ID 목록 (선택, 포지션별 필터링)", example = "[1, 2, 3]")
            @RequestParam(value = "positionIds", required = false) List<Long> positionIds
    ) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<ScheduleResponse> responses = scheduleService.getMySchedules(userId, start, end, teamId, positionIds);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("내 일정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long getUserIdFromHeader(String authorizationHeader) {
        return jwtHelper.getUserIdFromHeader(authorizationHeader);
    }

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


