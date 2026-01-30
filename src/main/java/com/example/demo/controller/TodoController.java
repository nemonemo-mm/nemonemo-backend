package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.todo.TodoCreateRequest;
import com.example.demo.dto.todo.TodoResponseDto;
import com.example.demo.dto.todo.TodoStatusUpdateRequest;
import com.example.demo.dto.todo.TodoUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.TodoService;
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

@Tag(name = "투두", description = "팀 투두 생성/수정/삭제 및 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "투두 생성", description = "팀 단위 투두를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponseDto.class))),
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
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 투두를 생성할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"투두 생성 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/todos")
    public ResponseEntity<?> createTodo(
            @Valid @RequestBody TodoCreateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            TodoResponseDto response = todoService.createTodo(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("투두 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "투두 수정", description = "투두를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 등)",
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
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 투두를 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "투두를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TODO_NOT_FOUND\",\"message\":\"투두를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"투두 수정 중 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/todos/{todoId}")
    public ResponseEntity<?> updateTodo(
            @Parameter(description = "투두 ID", example = "1")
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            TodoResponseDto response = todoService.updateTodo(userId, todoId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse("투두 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "투두 완료 여부 수정", description = "투두의 완료 여부만 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"상태는 필수입니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 투두를 수정할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "투두를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TODO_NOT_FOUND\",\"message\":\"투두를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"투두 완료 여부 수정 중 오류가 발생했습니다.\"}")))
    })
    @PatchMapping("/todos/{todoId}/status")
    public ResponseEntity<?> updateTodoStatus(
            @Parameter(description = "투두 ID", example = "1")
            @PathVariable Long todoId,
            @Valid @RequestBody TodoStatusUpdateRequest request
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            TodoResponseDto response = todoService.updateTodoStatus(userId, todoId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message != null && message.contains("투두를 찾을 수 없습니다")) {
                return createErrorResponse("TODO_NOT_FOUND", message, HttpStatus.NOT_FOUND);
            } else if (message != null && message.contains("팀원이 아닌")) {
                return createErrorResponse("FORBIDDEN", message, HttpStatus.FORBIDDEN);
            }
            return createErrorResponse("투두 완료 여부 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return createErrorResponse("투두 완료 여부 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "투두 삭제", description = "투두를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 투두를 삭제할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "투두를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TODO_NOT_FOUND\",\"message\":\"투두를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"투두 삭제 중 오류가 발생했습니다.\"}")))
    })
    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<?> deleteTodo(
            @Parameter(description = "투두 ID", example = "1")
            @PathVariable Long todoId
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            todoService.deleteTodo(userId, todoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return createErrorResponse("투두 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 투두 조회", description = "특정 팀의 기간 내 투두 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TodoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (팀원이 아님)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀원이 아닌 사용자는 팀 투두를 조회할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 투두 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/teams/{teamId}/todos")
    public ResponseEntity<?> getTeamTodos(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "조회 시작일시 (ISO 8601)", example = "2025-01-01T00:00:00Z")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "조회 종료일시 (ISO 8601)", example = "2025-01-31T23:59:59Z")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<TodoResponseDto> responses = todoService.getTeamTodos(userId, teamId, start, end);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return createErrorResponse("팀 투두 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "내 투두 조회", description = "개인 화면용으로, 내가 담당자로 포함된 투두만 기간 내 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TodoResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"내 투두 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/me/todos")
    public ResponseEntity<?> getMyTodos(
            @Parameter(description = "조회 시작일시 (ISO 8601)", example = "2025-01-01T00:00:00Z")
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "조회 종료일시 (ISO 8601)", example = "2025-01-31T23:59:59Z")
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }
            List<TodoResponseDto> responses = todoService.getMyTodos(userId, start, end);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return createErrorResponse("내 투두 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
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

    private ResponseEntity<ErrorResponse> createErrorResponse(String code, String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(code, message);
        return ResponseEntity.status(status).body(error);
    }
}


