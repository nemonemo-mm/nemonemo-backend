package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.notice.NoticeCreateRequest;
import com.example.demo.dto.notice.NoticeResponse;
import com.example.demo.dto.notice.NoticeUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.NoticeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공지", description = "팀 공지 생성/수정/삭제 및 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "공지 생성", description = "팀 단위 공지를 생성합니다. 공지 생성 시 팀의 모든 멤버에게 알림이 전송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"제목은 필수입니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 공지를 생성할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"공지 생성 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/teams/{teamId}/notices")
    public ResponseEntity<?> createNotice(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            NoticeResponse response = noticeService.createNotice(userId, teamId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("공지 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 공지 목록 조회", description = "특정 팀의 공지 목록을 조회합니다. 최신순으로 정렬됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NoticeResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 공지를 조회할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 공지 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/teams/{teamId}/notices")
    public ResponseEntity<?> getTeamNotices(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<NoticeResponse> responses = noticeService.getTeamNotices(userId, teamId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return createErrorResponse("팀 공지 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지 상세 조회", description = "특정 공지의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 공지를 조회할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"NOTICE_NOT_FOUND\",\"message\":\"공지를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"공지 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/teams/{teamId}/notices/{noticeId}")
    public ResponseEntity<?> getNotice(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "공지 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            NoticeResponse response = noticeService.getNotice(userId, teamId, noticeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("공지 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지 수정", description = "공지를 수정합니다. 작성자만 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"유효하지 않은 요청입니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님 또는 팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"공지 작성자만 수정할 수 있습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"NOTICE_NOT_FOUND\",\"message\":\"공지를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"공지 수정 중 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/teams/{teamId}/notices/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "공지 ID", example = "1")
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            NoticeResponse response = noticeService.updateNotice(userId, teamId, noticeId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("공지 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지 삭제", description = "공지를 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님 또는 팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"공지 작성자만 삭제할 수 있습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"NOTICE_NOT_FOUND\",\"message\":\"공지를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"공지 삭제 중 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/teams/{teamId}/notices/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "공지 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            noticeService.deleteNotice(userId, teamId, noticeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createErrorResponse("공지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        ErrorResponse error = new ErrorResponse("UNAUTHORIZED", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", message);
        return ResponseEntity.status(status).body(error);
    }
}

