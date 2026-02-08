package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.schedule.ScheduleCreateRequest;
import com.example.demo.dto.schedule.ScheduleResponseDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "제목 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"제목은 필수입니다.\"}"),
                                    @ExampleObject(name = "날짜 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"시작 일시와 종료 일시는 필수입니다.\"}"),
                                    @ExampleObject(name = "날짜 순서", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"종료 일시는 시작 일시보다 이후여야 합니다.\"}"),
                                    @ExampleObject(name = "반복 타입", value = "{\"code\":\"INVALID_REPEAT_TYPE\",\"message\":\"유효하지 않은 반복 유형입니다. 가능한 값: NONE, DAILY, WEEKLY, MONTHLY, YEARLY (입력값: INVALID)\"}"),
                                    @ExampleObject(name = "반복 설정", value = "{\"code\":\"INVALID_REPEAT_CONFIG\",\"message\":\"repeatType이 NONE일 때는 반복 관련 필드를 보낼 수 없습니다.\"}"),
                                    @ExampleObject(name = "반복 간격", value = "{\"code\":\"INVALID_REPEAT_CONFIG\",\"message\":\"repeatInterval은 1 이상이어야 합니다.\"}"),
                                    @ExampleObject(name = "참석자 없음", value = "{\"code\":\"INVALID_MEMBER_IDS\",\"message\":\"일부 팀원 ID가 유효하지 않습니다.\"}"),
                                    @ExampleObject(name = "참석자 팀 불일치", value = "{\"code\":\"INVALID_MEMBER_IDS\",\"message\":\"팀원이 해당 팀에 속하지 않습니다.\"}"),
                                    @ExampleObject(name = "포지션 없음", value = "{\"code\":\"INVALID_POSITION_IDS\",\"message\":\"일부 포지션 ID가 유효하지 않습니다.\"}"),
                                    @ExampleObject(name = "포지션 팀 불일치", value = "{\"code\":\"INVALID_POSITION_IDS\",\"message\":\"포지션이 해당 팀에 속하지 않습니다.\"}")
                            })),
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
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            ScheduleResponseDto response = scheduleService.createSchedule(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            log.error("스케줄 생성 중 예상치 못한 오류 발생: userId={}, error={}", 
                    jwtHelper.getCurrentUserId(), e.getMessage(), e);
            return createErrorResponse("스케줄 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "스케줄 수정", description = "스케줄을 수정합니다. 반복 스케줄의 경우 scope 파라미터로 적용 범위를 선택할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "제목 길이", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"제목은 최대 20자까지 입력 가능합니다.\"}"),
                                    @ExampleObject(name = "날짜 순서", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"종료 일시는 시작 일시보다 이후여야 합니다.\"}"),
                                    @ExampleObject(name = "반복 타입", value = "{\"code\":\"INVALID_REPEAT_TYPE\",\"message\":\"유효하지 않은 반복 유형입니다. 가능한 값: NONE, DAILY, WEEKLY, MONTHLY, YEARLY (입력값: INVALID)\"}"),
                                    @ExampleObject(name = "반복 설정", value = "{\"code\":\"INVALID_REPEAT_CONFIG\",\"message\":\"repeatType이 NONE일 때는 반복 관련 필드를 보낼 수 없습니다.\"}"),
                                    @ExampleObject(name = "반복 간격", value = "{\"code\":\"INVALID_REPEAT_CONFIG\",\"message\":\"repeatInterval은 1 이상이어야 합니다.\"}"),
                                    @ExampleObject(name = "참석자 없음", value = "{\"code\":\"INVALID_MEMBER_IDS\",\"message\":\"일부 팀원 ID가 유효하지 않습니다.\"}"),
                                    @ExampleObject(name = "참석자 팀 불일치", value = "{\"code\":\"INVALID_MEMBER_IDS\",\"message\":\"팀원이 해당 팀에 속하지 않습니다.\"}"),
                                    @ExampleObject(name = "포지션 없음", value = "{\"code\":\"INVALID_POSITION_IDS\",\"message\":\"일부 포지션 ID가 유효하지 않습니다.\"}"),
                                    @ExampleObject(name = "포지션 팀 불일치", value = "{\"code\":\"INVALID_POSITION_IDS\",\"message\":\"포지션이 해당 팀에 속하지 않습니다.\"}")
                            })),
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
            @Parameter(description = "스케줄 ID", example = "1")
            @PathVariable Long scheduleId,
            @Parameter(description = "반복 스케줄 수정 범위 (기본값: ALL)", example = "ALL")
            @RequestParam(name = "scope", required = false, defaultValue = "ALL") RepeatScope scope,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            ScheduleResponseDto response = scheduleService.updateSchedule(userId, scheduleId, request, scope);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            log.error("스케줄 수정 중 예상치 못한 오류 발생: scheduleId={}, userId={}, error={}", 
                    scheduleId, jwtHelper.getCurrentUserId(), e.getMessage(), e);
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
            @Parameter(description = "스케줄 ID", example = "1")
            @PathVariable Long scheduleId,
            @Parameter(description = "반복 스케줄 삭제 범위 (기본값: ALL)", example = "THIS_ONLY")
            @RequestParam(name = "scope", required = false, defaultValue = "ALL") RepeatScope scope
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            scheduleService.deleteSchedule(userId, scheduleId, scope);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createErrorResponse("스케줄 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 스케줄 조회", description = "특정 팀의 기간 내 스케줄 목록을 조회합니다. positionIds 파라미터로 포지션별 필터링이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (날짜 형식 오류 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "날짜 형식", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"잘못된 날짜 형식입니다. ISO 8601 형식(예: 2025-01-01T00:00:00Z)을 사용해주세요.\"}"),
                                    @ExampleObject(name = "날짜 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"시작일시와 종료일시는 필수입니다.\"}")
                            })),
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
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<ScheduleResponseDto> responses = scheduleService.getTeamSchedules(userId, teamId, start, end, positionIds);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return createErrorResponse("팀 일정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "내 스케줄 조회", description = "개인 화면용으로, 내가 참석자로 포함된 스케줄만 기간 내 조회합니다. teamId로 특정 팀 필터링, positionIds로 포지션별 필터링이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScheduleResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (날짜 형식 오류 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "날짜 형식", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"잘못된 날짜 형식입니다. ISO 8601 형식(예: 2025-01-01T00:00:00Z)을 사용해주세요.\"}"),
                                    @ExampleObject(name = "날짜 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"시작일시와 종료일시는 필수입니다.\"}")
                            })),
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
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<ScheduleResponseDto> responses = scheduleService.getMySchedules(userId, start, end, teamId, positionIds);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return createErrorResponse("내 일정 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음 등을 구분)
     */
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        log.debug("IllegalArgumentException 발생: {}", message);
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "SCHEDULE_NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            if ("FORBIDDEN".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("SCHEDULE_NOT_FOUND".equals(errorCode) || "TEAM_NOT_FOUND".equals(errorCode)) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code(errorCode)
                            .message(cleanMessage)
                            .build());
        }
        
        // 에러 코드가 없는 경우 메시지로 판단
        String code = "INVALID_REQUEST";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        if (message != null) {
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("팀원이 아닌")) {
                code = "FORBIDDEN";
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                code = "SCHEDULE_NOT_FOUND";
                status = HttpStatus.NOT_FOUND;
            }
        }
        
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message != null ? message : "잘못된 요청입니다.")
                        .build());
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        String code = status == HttpStatus.INTERNAL_SERVER_ERROR ? "INTERNAL_SERVER_ERROR" : "INVALID_REQUEST";
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .build());
    }
    
    /**
     * 인증 실패 응답 생성
     */
    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .code("UNAUTHORIZED")
                        .message(message)
                        .build());
    }
}


