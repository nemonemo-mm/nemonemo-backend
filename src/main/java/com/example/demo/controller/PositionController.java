package com.example.demo.controller;

import com.example.demo.service.PositionService;
import com.example.demo.dto.team.PositionCreateRequest;
import com.example.demo.dto.team.PositionResponse;
import com.example.demo.dto.team.PositionUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            content = @Content(schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPositionList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<PositionResponse> positions = positionService.getPositionList(userId, id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", positions);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 생성", description = "새로운 포지션을 생성합니다. 팀장만 생성 가능하며, 최대 6개까지 생성 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 생성 성공",
            content = @Content(schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 중복 이름, 최대 개수 초과)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPosition(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PositionCreateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            PositionResponse response = positionService.createPosition(userId, id, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", response);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 수정", description = "포지션 정보를 수정합니다. 팀장만 수정 가능하며, 기본 포지션의 이름은 변경할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 수정 성공",
            content = @Content(schema = @Schema(implementation = PositionResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 기본 포지션 이름 변경 시도, 중복 이름)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀 또는 포지션을 찾을 수 없음")
    })
    @PatchMapping("/{positionId}")
    public ResponseEntity<Map<String, Object>> updatePosition(
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
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", response);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("포지션 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "포지션 삭제", description = "포지션을 삭제합니다. 팀장만 삭제 가능하며, 기본 포지션은 삭제할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포지션 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (기본 포지션 삭제 시도)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀 또는 포지션을 찾을 수 없음")
    })
    @DeleteMapping("/{positionId}")
    public ResponseEntity<Map<String, Object>> deletePosition(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "포지션 ID", required = true) @PathVariable Long positionId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            positionService.deletePosition(userId, id, positionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", null);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
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
    private ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            if ("FORBIDDEN".equals(errorCode)) {
                return createErrorResponseWithCode("FORBIDDEN", cleanMessage, HttpStatus.FORBIDDEN);
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
                return createErrorResponseWithCode("NOT_FOUND", cleanMessage, HttpStatus.NOT_FOUND);
            } else {
                return createErrorResponseWithCode(errorCode, cleanMessage, HttpStatus.BAD_REQUEST);
            }
        }
        
        // 에러 코드가 없는 경우 메시지로 판단
        if (message != null) {
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("멤버만")) {
                String cleanMessage = message.replace("FORBIDDEN:", "").trim();
                return createErrorResponseWithCode("FORBIDDEN", cleanMessage, HttpStatus.FORBIDDEN);
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                return createErrorResponseWithCode("NOT_FOUND", message, HttpStatus.NOT_FOUND);
            } else if (message.contains("필수")) {
                return createErrorResponseWithCode("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
            } else if (message.contains("최대") || message.contains("길이")) {
                return createErrorResponseWithCode("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
            }
        }
        
        // 기본값
        return createErrorResponseWithCode("INVALID_REQUEST", message != null ? message : "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 특정 에러 코드로 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponseWithCode(String code, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", status == HttpStatus.INTERNAL_SERVER_ERROR ? "INTERNAL_SERVER_ERROR" : "INVALID_REQUEST");
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 인증 실패 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "UNAUTHORIZED");
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
