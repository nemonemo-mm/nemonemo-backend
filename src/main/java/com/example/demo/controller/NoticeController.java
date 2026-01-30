package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.notice.NoticeCreateRequest;
import com.example.demo.dto.notice.NoticeResponse;
import com.example.demo.dto.notice.NoticeUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.NoticeService;
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


@Tag(name = "공지", description = "팀 공지 생성/수정/삭제 및 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "공지 생성", description = "팀 단위 공지를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"teamId\": 1,\n" +
                                    "  \"teamName\": \"NemoNemo 팀\",\n" +
                                    "  \"content\": \"다음 주 월요일부터 프로젝트 일정이 변경됩니다.\",\n" +
                                    "  \"authorId\": 1,\n" +
                                    "  \"authorName\": \"홍길동\",\n" +
                                    "  \"createdAt\": \"2024-01-15T10:30:00.000Z\",\n" +
                                    "  \"updatedAt\": \"2024-01-15T10:30:00.000Z\"\n" +
                                    "}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/teams/{teamId}/notices")
    public ResponseEntity<?> createNotice(
            @PathVariable Long teamId,
            @Valid @RequestBody NoticeCreateRequest request) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            NoticeResponse response = noticeService.createNotice(userId, teamId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("공지 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "최신 공지 조회", description = "특정 팀의 가장 최신 공지사항을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최신 공지 조회 성공 (공지가 없으면 null 반환)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class),
                            examples = {
                                    @ExampleObject(name = "공지 있음", value = "{\n" +
                                            "  \"id\": 1,\n" +
                                            "  \"teamId\": 1,\n" +
                                            "  \"teamName\": \"NemoNemo 팀\",\n" +
                                            "  \"content\": \"다음 주 월요일부터 프로젝트 일정이 변경됩니다.\",\n" +
                                            "  \"authorId\": 1,\n" +
                                            "  \"authorName\": \"홍길동\",\n" +
                                            "  \"createdAt\": \"2024-01-15T10:30:00.000Z\",\n" +
                                            "  \"updatedAt\": \"2024-01-15T10:30:00.000Z\"\n" +
                                            "}"),
                                    @ExampleObject(name = "공지 없음", value = "null")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/teams/{teamId}/notices/latest")
    public ResponseEntity<?> getLatestNotice(@PathVariable Long teamId) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            NoticeResponse response = noticeService.getLatestNotice(userId, teamId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("최신 공지 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지 수정", description = "공지를 수정합니다. 작성자만 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeResponse.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"id\": 1,\n" +
                                    "  \"teamId\": 1,\n" +
                                    "  \"teamName\": \"NemoNemo 팀\",\n" +
                                    "  \"content\": \"프로젝트 일정이 변경되었습니다. 자세한 내용은 팀 채팅을 확인해주세요.\",\n" +
                                    "  \"authorId\": 1,\n" +
                                    "  \"authorName\": \"홍길동\",\n" +
                                    "  \"createdAt\": \"2024-01-15T10:30:00.000Z\",\n" +
                                    "  \"updatedAt\": \"2024-01-15T11:00:00.000Z\"\n" +
                                    "}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 수정 가능)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/teams/{teamId}/notices/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long teamId,
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            NoticeResponse response = noticeService.updateNotice(userId, teamId, noticeId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("공지 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "공지 삭제", description = "공지를 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공지 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 삭제 가능)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "공지를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/teams/{teamId}/notices/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable Long teamId,
            @PathVariable Long noticeId) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            noticeService.deleteNotice(userId, teamId, noticeId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("공지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음 등을 구분)
     */
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOTICE_NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            if ("FORBIDDEN".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("NOTICE_NOT_FOUND".equals(errorCode) || "TEAM_NOT_FOUND".equals(errorCode)) {
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
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("작성자만")) {
                code = "FORBIDDEN";
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                code = "NOT_FOUND";
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

