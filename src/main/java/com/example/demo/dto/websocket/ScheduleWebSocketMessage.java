package com.example.demo.dto.websocket;

import com.example.demo.dto.schedule.ScheduleResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 스케줄 관련 WebSocket 메시지
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleWebSocketMessage {
    /**
     * 액션 타입: CREATED, UPDATED, DELETED
     */
    private String action;
    
    /**
     * 스케줄 정보 (DELETED의 경우 id만 포함)
     */
    private ScheduleResponseDto schedule;
    
    /**
     * 메시지 전송 시간
     */
    private LocalDateTime timestamp;
}

