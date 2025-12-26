package com.example.demo.dto.websocket;

import com.example.demo.dto.team.TeamMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 팀 멤버 관련 WebSocket 메시지
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberWebSocketMessage {
    /**
     * 액션 타입: JOINED, LEFT
     */
    private String action;
    
    /**
     * 멤버 정보 (LEFT의 경우 memberId, userId만 포함)
     */
    private TeamMemberResponse member;
    
    /**
     * 팀 ID
     */
    private Long teamId;
    
    /**
     * 메시지 전송 시간
     */
    private LocalDateTime timestamp;
}


