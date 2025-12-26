package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 연결 상태 확인용 컨트롤러
 */
@Tag(name = "WebSocket", description = "WebSocket 연결 상태 확인 API")
@RestController
@RequestMapping("/api/v1/websocket")
public class WebSocketController {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${websocket.allowed-origins:}")
    private String allowedOrigins;

    @Operation(
        summary = "WebSocket 연결 정보 조회",
        description = "WebSocket 엔드포인트 및 연결 정보를 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "WebSocket 정보 조회 성공",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = WebSocketInfoResponse.class))
        )
    })
    @GetMapping("/info")
    public ResponseEntity<WebSocketInfoResponse> getWebSocketInfo() {
        // 현재 서버 URL 구성
        String wsUrl = "ws://localhost:" + serverPort + "/ws";
        String wsSockJsUrl = "http://localhost:" + serverPort + "/ws";
        
        // 구독 가능한 토픽 목록
        Map<String, String> topics = new HashMap<>();
        topics.put("스케줄 변경", "/topic/team/{teamId}/schedules");
        topics.put("팀 멤버 변경", "/topic/team/{teamId}/members");
        
        WebSocketInfoResponse response = WebSocketInfoResponse.builder()
                .status("active")
                .endpoint("/ws")
                .protocol("STOMP over WebSocket")
                .sockJsEnabled(true)
                .websocketUrl(wsUrl)
                .sockJsUrl(wsSockJsUrl)
                .allowedOrigins(allowedOrigins != null && !allowedOrigins.trim().isEmpty() 
                    ? allowedOrigins 
                    : "모든 Origin 허용 (개발 환경)")
                .topics(topics)
                .messageBrokerPrefixes(new String[]{"/topic", "/queue"})
                .applicationDestinationPrefix("/app/v1")
                .checkedAt(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "WebSocket 연결 정보")
    public static class WebSocketInfoResponse {
        @Schema(description = "WebSocket 상태", example = "active")
        private String status;
        
        @Schema(description = "WebSocket 엔드포인트", example = "/ws")
        private String endpoint;
        
        @Schema(description = "사용 프로토콜", example = "STOMP over WebSocket")
        private String protocol;
        
        @Schema(description = "SockJS 지원 여부", example = "true")
        private Boolean sockJsEnabled;
        
        @Schema(description = "WebSocket 연결 URL", example = "ws://localhost:8080/ws")
        private String websocketUrl;
        
        @Schema(description = "SockJS 연결 URL", example = "http://localhost:8080/ws")
        private String sockJsUrl;
        
        @Schema(description = "허용된 Origin", example = "모든 Origin 허용")
        private String allowedOrigins;
        
        @Schema(description = "구독 가능한 토픽 목록")
        private Map<String, String> topics;
        
        @Schema(description = "메시지 브로커 프리픽스", example = "[\"/topic\", \"/queue\"]")
        private String[] messageBrokerPrefixes;
        
        @Schema(description = "애플리케이션 목적지 프리픽스", example = "/app/v1")
        private String applicationDestinationPrefix;
        
        @Schema(description = "확인 시각")
        private LocalDateTime checkedAt;
    }
}




