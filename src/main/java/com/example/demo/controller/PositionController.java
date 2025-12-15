package com.example.demo.controller;

import com.example.demo.service.PositionService;
import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.team.PositionCreateRequest;
import com.example.demo.dto.team.PositionDeleteResponse;
import com.example.demo.dto.team.PositionResponse;
import com.example.demo.dto.team.PositionUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "포지션 관리", description = "포지션 생성, 수정, 조회, 삭제 API")
@RestController
@RequestMapping("/api/v1/teams/{id}/positions")
@RequiredArgsConstructor
public class PositionController {
    
    private final PositionService positionService;
    private final JwtAuthenticationHelper jwtHelper;
    
    @Operation(summary = "포지션 목록 조회", description = "팀의 포지션 목록을 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀원만 조회 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getPositionList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<PositionResponse> positions = positionService.getPositionList(userId, id);
            return ResponseEntity.ok(positions);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 생성", description = "새로운 포지션을 생성합니다. 팀장만 생성 가능하며, 최대 6개까지 생성 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 생성 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 중복 이름, 최대 개수 초과) - 에러 코드: VALIDATION_ERROR, INVALID_REQUEST",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "포지션 이름 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"포지션 이름은 필수입니다.\"}"),
                    @ExampleObject(name = "포지션 이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"포지션 이름은 최대 10자까지 입력 가능합니다.\"}"),
                    @ExampleObject(name = "색상 코드 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"색상 코드는 최대 9자까지 입력 가능합니다.\"}"),
                    @ExampleObject(name = "중복 이름", value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"이미 존재하는 포지션 이름입니다.\"}"),
                    @ExampleObject(name = "최대 개수 초과", value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"포지션은 최대 6개까지 추가할 수 있습니다. (기본값 MEMBER 포함 시 7개)\"}")
                })),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 생성 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createPosition(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PositionCreateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            PositionResponse response = positionService.createPosition(userId, id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 수정", description = "포지션 정보를 수정합니다. 팀장만 수정 가능하며, 기본 포지션의 이름은 변경할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 기본 포지션 이름 변경 시도, 중복 이름) - 에러 코드: VALIDATION_ERROR, INVALID_REQUEST",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "포지션 이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"포지션 이름은 최대 10자까지 입력 가능합니다.\"}"),
                    @ExampleObject(name = "색상 코드 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"색상 코드는 최대 9자까지 입력 가능합니다.\"}"),
                    @ExampleObject(name = "기본 포지션 이름 변경 불가", value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"기본 포지션의 이름은 변경할 수 없습니다.\"}"),
                    @ExampleObject(name = "중복 이름", value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"이미 존재하는 포지션 이름입니다.\"}")
                })),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 수정 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀 또는 포지션을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND, POSITION_NOT_FOUND",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{positionId}")
    public ResponseEntity<?> updatePosition(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "포지션 ID", required = true) @PathVariable Long positionId,
            @Valid @RequestBody PositionUpdateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            PositionResponse response = positionService.updatePosition(userId, id, positionId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 삭제", description = "포지션을 삭제합니다. 팀장만 삭제 가능하며, 기본 포지션은 삭제할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 삭제 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionDeleteResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (기본 포지션 삭제 시도) - 에러 코드: INVALID_REQUEST",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 삭제 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "팀 또는 포지션을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND, POSITION_NOT_FOUND",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{positionId}")
    public ResponseEntity<?> deletePosition(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "포지션 ID", required = true) @PathVariable Long positionId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            PositionDeleteResponse response = positionService.deletePosition(userId, id, positionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Authorization 헤더에서 사용자 ID를 추출합니다.
     */
    private Long getUserIdFromHeader(String authorizationHeader) {
        return jwtHelper.getUserIdFromHeader(authorizationHeader);
    }
    
    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음 등을 구분)
     */
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            if ("FORBIDDEN".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
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
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("멤버만")) {
                code = "FORBIDDEN";
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                code = "NOT_FOUND";
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("필수")) {
                code = "VALIDATION_ERROR";
            } else if (message.contains("최대") || message.contains("길이")) {
                code = "VALIDATION_ERROR";
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
