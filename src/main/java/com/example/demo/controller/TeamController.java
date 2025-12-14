package com.example.demo.controller;

import com.example.demo.service.TeamService;
import com.example.demo.dto.team.InviteCodeResponse;
import com.example.demo.dto.team.TeamCreateRequest;
import com.example.demo.dto.team.TeamDetailResponse;
import com.example.demo.dto.team.TeamJoinRequest;
import com.example.demo.dto.team.TeamMemberListItemResponse;
import com.example.demo.dto.team.TeamMemberResponse;
import com.example.demo.dto.team.TeamMemberUpdateRequest;
import com.example.demo.dto.team.TeamUpdateRequest;
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

@Tag(name = "팀 관리", description = "팀 생성, 수정, 조회, 삭제 API")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    private final JwtAuthenticationHelper jwtHelper;
    
    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다. 인증된 사용자는 누구나 팀을 생성할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 생성 성공",
            content = @Content(schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TeamCreateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.createTeam(userId, request);
            
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
            return createErrorResponse("팀 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 수정", description = "팀 정보를 수정합니다. 팀장만 수정 가능하며, 부분 수정이 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 수정 성공",
            content = @Content(schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.updateTeam(userId, id, request);
            
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
            return createErrorResponse("팀 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 상세 조회", description = "팀 상세 정보를 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 조회 성공",
            content = @Content(schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTeamDetail(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.getTeamDetail(userId, id);
            
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
            return createErrorResponse("팀 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 목록 조회", description = "사용자가 속한 팀 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTeamList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<TeamDetailResponse> teams = teamService.getTeamList(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", teams);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return createErrorResponse("팀 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 삭제", description = "팀을 삭제합니다. 팀장만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            teamService.deleteTeam(userId, id);
            
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
            return createErrorResponse("팀 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "초대 코드 조회", description = "팀의 초대 코드를 조회합니다. 팀장만 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초대 코드 조회 성공",
            content = @Content(schema = @Schema(implementation = InviteCodeResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @GetMapping("/{id}/invite")
    public ResponseEntity<Map<String, Object>> getInviteCode(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            InviteCodeResponse response = teamService.getInviteCode(userId, id);
            
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
            return createErrorResponse("초대 코드 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 참여", description = "초대 코드를 사용하여 팀에 참여합니다. 팀원만 참여 가능하며, 팀장은 참여할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 참여 성공",
            content = @Content(schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 멤버, 유효하지 않은 초대 코드)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장은 참여 불가)"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TeamJoinRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberResponse response = teamService.joinTeam(userId, request);
            
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
            return createErrorResponse("팀 참여 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 탈퇴", description = "팀에서 탈퇴합니다. 팀원만 탈퇴 가능하며, 팀장은 탈퇴할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (팀장은 탈퇴 불가)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 또는 멤버가 아님")
    })
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<Map<String, Object>> leaveTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            teamService.leaveTeam(userId, id);
            
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
            return createErrorResponse("팀 탈퇴 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 목록 조회", description = "팀원 목록을 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = TeamMemberListItemResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<Map<String, Object>> getTeamMemberList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<TeamMemberListItemResponse> members = teamService.getTeamMemberList(userId, id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", members);
            result.put("meta", null);
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀원 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 상세 조회", description = "특정 팀원의 상세 정보를 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음")
    })
    @GetMapping("/{id}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> getTeamMemberDetail(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "팀원 ID", required = true) @PathVariable Long memberId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberResponse response = teamService.getTeamMemberDetail(userId, id, memberId);
            
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
            return createErrorResponse("팀원 상세 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 정보 수정", description = "팀원 정보를 수정합니다. 본인 정보 수정은 모두 가능하며, 다른 팀원 정보 수정은 팀장만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 포지션)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음")
    })
    @PatchMapping("/{id}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> updateTeamMember(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "팀원 ID", required = true) @PathVariable Long memberId,
            @Valid @RequestBody TeamMemberUpdateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberResponse response = teamService.updateTeamMember(userId, id, memberId, request);
            
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
            return createErrorResponse("팀원 정보 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 삭제", description = "팀원을 삭제합니다. 팀장만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음")
    })
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> deleteTeamMember(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "팀원 ID", required = true) @PathVariable Long memberId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            teamService.deleteTeamMember(userId, id, memberId);
            
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
            return createErrorResponse("팀원 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
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
