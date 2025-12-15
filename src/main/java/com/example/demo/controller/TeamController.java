package com.example.demo.controller;

import com.example.demo.service.TeamService;
import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.team.InviteCodeResponse;
import com.example.demo.dto.team.TeamCreateRequest;
import com.example.demo.dto.team.TeamDeleteResponse;
import com.example.demo.dto.team.TeamDetailResponse;
import com.example.demo.dto.team.TeamJoinRequest;
import com.example.demo.dto.team.TeamLeaveResponse;
import com.example.demo.dto.team.TeamMemberDeleteResponse;
import com.example.demo.dto.team.TeamMemberListItemResponse;
import com.example.demo.dto.team.TeamMemberResponse;
import com.example.demo.dto.team.TeamMemberUpdateRequest;
import com.example.demo.dto.team.TeamUpdateRequest;
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 팀 이름 필수 또는 최대 길이 초과) - 에러 코드: VALIDATION_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "팀 이름 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"팀 이름은 필수입니다.\"}"),
                    @ExampleObject(name = "팀 이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"팀 이름은 최대 10자까지 입력 가능합니다.\"}")
                })),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @PostMapping
    public ResponseEntity<?> createTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TeamCreateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.createTeam(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 수정", description = "팀 정보를 수정합니다. 팀장만 수정 가능하며, 부분 수정이 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 팀 이름 길이 초과) - 에러 코드: VALIDATION_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "팀 이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"팀 이름은 최대 10자까지 입력 가능합니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 수정 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 수정할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.updateTeam(userId, id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 상세 조회", description = "팀 상세 정보를 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀원만 조회 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원만 조회할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamDetail(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDetailResponse response = teamService.getTeamDetail(userId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 목록 조회", description = "사용자가 속한 팀 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @GetMapping
    public ResponseEntity<?> getTeamList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<TeamDetailResponse> teams = teamService.getTeamList(userId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            return createErrorResponse("팀 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 삭제", description = "팀을 삭제합니다. 팀장만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 삭제 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDeleteResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 삭제 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 삭제할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamDeleteResponse response = teamService.deleteTeam(userId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "초대 코드 조회", description = "팀의 초대 코드를 조회합니다. 팀장만 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초대 코드 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InviteCodeResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 조회 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 조회할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/{id}/invite")
    public ResponseEntity<?> getInviteCode(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            InviteCodeResponse response = teamService.getInviteCode(userId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("초대 코드 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 참여", description = "초대 코드를 사용하여 팀에 참여합니다. 팀원만 참여 가능하며, 팀장은 참여할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 참여 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 멤버, 유효하지 않은 초대 코드) - 에러 코드: ALREADY_MEMBER, INVALID_INVITE_CODE",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ALREADY_MEMBER\",\"message\":\"이미 팀 멤버입니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장은 참여 불가) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장은 참여할 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TeamJoinRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberResponse response = teamService.joinTeam(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 참여 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀 탈퇴", description = "팀에서 탈퇴합니다. 팀원만 탈퇴 가능하며, 팀장은 탈퇴할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀 탈퇴 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamLeaveResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (팀장은 탈퇴 불가) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장은 탈퇴할 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 또는 멤버가 아님 - 에러 코드: TEAM_NOT_FOUND, NOT_A_MEMBER",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<?> leaveTeam(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamLeaveResponse response = teamService.leaveTeam(userId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 탈퇴 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 목록 조회", description = "팀원 목록을 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberListItemResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀원만 조회 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원만 조회할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getTeamMemberList(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            List<TeamMemberListItemResponse> members = teamService.getTeamMemberList(userId, id);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀원 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 상세 조회", description = "특정 팀원의 상세 정보를 조회합니다. 팀원 모두 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 상세 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀원만 조회 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원만 조회할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND, TEAM_MEMBER_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_MEMBER_NOT_FOUND\",\"message\":\"팀원을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> getTeamMemberDetail(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "팀원 ID", required = true) @PathVariable Long memberId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberResponse response = teamService.getTeamMemberDetail(userId, id, memberId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀원 상세 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 정보 수정", description = "팀원 정보를 수정합니다. 본인 정보 수정은 모두 가능하며, 다른 팀원 정보 수정은 팀장만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 정보 수정 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 유효하지 않은 포지션) - 에러 코드: VALIDATION_ERROR, POSITION_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "닉네임 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"닉네임은 최대 10자까지 입력 가능합니다.\"}"),
                    @ExampleObject(name = "포지션 없음", value = "{\"code\":\"POSITION_NOT_FOUND\",\"message\":\"포지션을 찾을 수 없습니다.\"}")
                })),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인 또는 팀장만 수정 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"본인 또는 팀장만 수정할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND, TEAM_MEMBER_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_MEMBER_NOT_FOUND\",\"message\":\"팀원을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> updateTeamMember(
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
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀원 정보 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "팀원 삭제", description = "팀원을 삭제합니다. 팀장만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "팀원 삭제 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberDeleteResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 삭제 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 삭제할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀 또는 팀원을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND, TEAM_MEMBER_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_MEMBER_NOT_FOUND\",\"message\":\"팀원을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> deleteTeamMember(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long id,
            @Parameter(description = "팀원 ID", required = true) @PathVariable Long memberId) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            
            TeamMemberDeleteResponse response = teamService.deleteTeamMember(userId, id, memberId);
            return ResponseEntity.ok(response);
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
