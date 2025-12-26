package com.example.demo.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FcmNotificationService {

    /**
     * 단일 디바이스에 알림 전송
     * @param deviceToken FCM 디바이스 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공 여부
     */
    public boolean sendNotification(String deviceToken, String title, String body, java.util.Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: token={}, response={}", deviceToken, response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송 실패: token={}, error={}", deviceToken, e.getMessage(), e);
            
            // 토큰이 유효하지 않은 경우 (만료, 삭제 등)
            if (e.getErrorCode().equals("invalid-argument") || 
                e.getErrorCode().equals("registration-token-not-registered")) {
                log.warn("유효하지 않은 FCM 토큰: {}", deviceToken);
            }
            return false;
        } catch (Exception e) {
            log.error("FCM 알림 전송 중 예상치 못한 오류: token={}", deviceToken, e);
            return false;
        }
    }

    /**
     * 여러 디바이스에 알림 전송
     * @param deviceTokens FCM 디바이스 토큰 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공한 토큰 수
     */
    public int sendNotificationToMultipleDevices(List<String> deviceTokens, String title, String body, java.util.Map<String, String> data) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String token : deviceTokens) {
            if (sendNotification(token, title, body, data)) {
                successCount++;
            }
        }
        log.info("FCM 알림 일괄 전송 완료: 전체={}, 성공={}", deviceTokens.size(), successCount);
        return successCount;
    }

    /**
     * 사용자에게 알림 전송 (사용자의 모든 디바이스)
     * @param deviceTokens 사용자의 디바이스 토큰 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공한 토큰 수
     */
    public int sendNotificationToUser(List<String> deviceTokens, String title, String body, java.util.Map<String, String> data) {
        return sendNotificationToMultipleDevices(deviceTokens, title, body, data);
    }

    /**
     * 스케줄 변경 알림 전송
     */
    public void sendScheduleChangeNotification(List<String> deviceTokens, String scheduleTitle, String teamName) {
        String title = "스케줄 변경 알림";
        String body = String.format("[%s] %s 스케줄이 변경되었습니다.", teamName, scheduleTitle);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "schedule_change");
        data.put("teamName", teamName);
        data.put("scheduleTitle", scheduleTitle);
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }

    /**
     * 스케줄 미리 알림 전송
     */
    public void sendSchedulePreNotification(List<String> deviceTokens, String scheduleTitle, String teamName, int minutesBefore) {
        String title = "스케줄 시작 알림";
        String body = String.format("[%s] %s 스케줄이 %d분 후 시작됩니다.", teamName, scheduleTitle, minutesBefore);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "schedule_pre");
        data.put("teamName", teamName);
        data.put("scheduleTitle", scheduleTitle);
        data.put("minutesBefore", String.valueOf(minutesBefore));
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }

    /**
     * 투두 변경 알림 전송
     */
    public void sendTodoChangeNotification(List<String> deviceTokens, String todoTitle, String teamName) {
        String title = "투두 변경 알림";
        String body = String.format("[%s] %s 투두가 변경되었습니다.", teamName, todoTitle);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "todo_change");
        data.put("teamName", teamName);
        data.put("todoTitle", todoTitle);
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }

    /**
     * 투두 마감 알림 전송
     */
    public void sendTodoDeadlineNotification(List<String> deviceTokens, String todoTitle, String teamName, int minutesBefore) {
        String title = "투두 마감 알림";
        String body = String.format("[%s] %s 투두가 %d분 후 마감됩니다.", teamName, todoTitle, minutesBefore);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "todo_deadline");
        data.put("teamName", teamName);
        data.put("todoTitle", todoTitle);
        data.put("minutesBefore", String.valueOf(minutesBefore));
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }

    /**
     * 공지 알림 전송
     */
    public void sendNoticeNotification(List<String> deviceTokens, String noticeTitle, String teamName) {
        String title = "공지 알림";
        String body = String.format("[%s] %s 공지가 등록되었습니다.", teamName, noticeTitle);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "notice");
        data.put("teamName", teamName);
        data.put("noticeTitle", noticeTitle);
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }

    /**
     * 팀원 입장/나가기 알림 전송
     */
    public void sendTeamMemberNotification(List<String> deviceTokens, String memberName, String teamName, boolean isJoin) {
        String title = "팀원 알림";
        String body = isJoin 
            ? String.format("[%s] %s님이 팀에 입장했습니다.", teamName, memberName)
            : String.format("[%s] %s님이 팀에서 나갔습니다.", teamName, memberName);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "team_member");
        data.put("teamName", teamName);
        data.put("memberName", memberName);
        data.put("action", isJoin ? "join" : "leave");
        
        sendNotificationToUser(deviceTokens, title, body, data);
    }
}











