package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class ExpoNotificationService {

    private static final String EXPO_PUSH_API_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate;

    public ExpoNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 단일 디바이스에 알림 전송
     * @param expoPushToken Expo Push Token (ExponentPushToken[...] 형식)
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공 여부
     */
    public boolean sendNotification(String expoPushToken, String title, String body, Map<String, String> data) {
        try {
            ExpoPushMessage message = ExpoPushMessage.builder()
                    .to(expoPushToken)
                    .title(title)
                    .body(body)
                    .data(data != null ? data : new HashMap<>())
                    .sound("default")
                    .priority("high")
                    .build();

            List<ExpoPushMessage> messages = Collections.singletonList(message);
            ExpoPushResponse response = sendPushNotifications(messages);

            if (response != null) {
                // 전체 요청 레벨 에러 확인
                if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                    log.error("Expo 알림 전송 실패 (요청 레벨): token={}, errors={}", expoPushToken, response.getErrors());
                    return false;
                }
                
                // 개별 메시지 결과 확인
                if (response.getData() != null && !response.getData().isEmpty()) {
                    ExpoPushTicket ticket = response.getData().get(0);
                    if ("ok".equals(ticket.getStatus())) {
                        log.info("Expo 알림 전송 성공: token={}", expoPushToken);
                        return true;
                    } else {
                        log.error("Expo 알림 전송 실패: token={}, status={}, message={}, details={}", 
                                expoPushToken, ticket.getStatus(), ticket.getMessage(), ticket.getDetails());
                        return false;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Expo 알림 전송 중 예상치 못한 오류: token={}", expoPushToken, e);
            return false;
        }
    }

    /**
     * 여러 디바이스에 알림 전송
     * @param expoPushTokens Expo Push Token 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공한 토큰 수
     */
    public int sendNotificationToMultipleDevices(List<String> expoPushTokens, String title, String body, Map<String, String> data) {
        if (expoPushTokens == null || expoPushTokens.isEmpty()) {
            return 0;
        }

        try {
            // Expo는 한 요청에 최대 100개까지 지원
            List<ExpoPushMessage> messages = new ArrayList<>();
            for (String token : expoPushTokens) {
                ExpoPushMessage message = ExpoPushMessage.builder()
                        .to(token)
                        .title(title)
                        .body(body)
                        .data(data != null ? data : new HashMap<>())
                        .sound("default")
                        .priority("high")
                        .build();
                messages.add(message);
            }

            // 100개씩 나누어 전송
            int successCount = 0;
            int batchSize = 100;
            for (int i = 0; i < messages.size(); i += batchSize) {
                int end = Math.min(i + batchSize, messages.size());
                List<ExpoPushMessage> batch = messages.subList(i, end);
                
                ExpoPushResponse response = sendPushNotifications(batch);
                
                if (response != null && response.getData() != null) {
                    int batchSuccess = (int) response.getData().stream()
                            .filter(ticket -> "ok".equals(ticket.getStatus()))
                            .count();
                    successCount += batchSuccess;
                }
            }
            
            log.info("Expo 알림 일괄 전송 완료: 전체={}, 성공={}", expoPushTokens.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("Expo 알림 일괄 전송 중 오류", e);
            return 0;
        }
    }

    /**
     * Expo Push API로 알림 전송
     * 문서: https://docs.expo.dev/push-notifications/sending-notifications/
     */
    private ExpoPushResponse sendPushNotifications(List<ExpoPushMessage> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("accept-encoding", "gzip, deflate");

        // 단일 메시지일 때는 객체로, 여러 메시지일 때는 배열로 전송
        Object requestBody;
        if (messages.size() == 1) {
            requestBody = messages.get(0);
        } else {
            requestBody = messages;
        }

        HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<ExpoPushResponse> response = restTemplate.exchange(
                    EXPO_PUSH_API_URL,
                    HttpMethod.POST,
                    request,
                    ExpoPushResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExpoPushResponse expoResponse = response.getBody();
                
                // errors 필드 확인 (전체 요청 레벨 에러)
                if (expoResponse.getErrors() != null && !expoResponse.getErrors().isEmpty()) {
                    log.error("Expo Push API 전체 요청 에러: {}", expoResponse.getErrors());
                }
                
                return expoResponse;
            }
            return null;
        } catch (Exception e) {
            log.error("Expo Push API 호출 실패", e);
            return null;
        }
    }

    /**
     * 사용자에게 알림 전송 (사용자의 모든 디바이스)
     * @param expoPushTokens 사용자의 Expo Push Token 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 전송 성공한 토큰 수
     */
    public int sendNotificationToUser(List<String> expoPushTokens, String title, String body, Map<String, String> data) {
        return sendNotificationToMultipleDevices(expoPushTokens, title, body, data);
    }

    /**
     * 스케줄 변경 알림 전송
     */
    public void sendScheduleChangeNotification(List<String> expoPushTokens, String scheduleTitle, String teamName) {
        String title = "스케줄 변경 알림";
        String body = String.format("[%s] %s 스케줄이 변경되었습니다.", teamName, scheduleTitle);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "schedule_change");
        data.put("teamName", teamName);
        data.put("scheduleTitle", scheduleTitle);
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    /**
     * 스케줄 미리 알림 전송
     */
    public void sendSchedulePreNotification(List<String> expoPushTokens, String scheduleTitle, String teamName, int minutesBefore) {
        String title = "스케줄 시작 알림";
        String body = String.format("[%s] %s 스케줄이 %d분 후 시작됩니다.", teamName, scheduleTitle, minutesBefore);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "schedule_pre");
        data.put("teamName", teamName);
        data.put("scheduleTitle", scheduleTitle);
        data.put("minutesBefore", String.valueOf(minutesBefore));
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    /**
     * 투두 변경 알림 전송
     */
    public void sendTodoChangeNotification(List<String> expoPushTokens, String todoTitle, String teamName) {
        String title = "투두 변경 알림";
        String body = String.format("[%s] %s 투두가 변경되었습니다.", teamName, todoTitle);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "todo_change");
        data.put("teamName", teamName);
        data.put("todoTitle", todoTitle);
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    /**
     * 투두 마감 알림 전송
     */
    public void sendTodoDeadlineNotification(List<String> expoPushTokens, String todoTitle, String teamName, int minutesBefore) {
        String title = "투두 마감 알림";
        String body = String.format("[%s] %s 투두가 %d분 후 마감됩니다.", teamName, todoTitle, minutesBefore);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "todo_deadline");
        data.put("teamName", teamName);
        data.put("todoTitle", todoTitle);
        data.put("minutesBefore", String.valueOf(minutesBefore));
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    /**
     * 공지 알림 전송
     */
    public void sendNoticeNotification(List<String> expoPushTokens, String noticeTitle, String teamName) {
        String title = "공지 알림";
        String body = String.format("[%s] %s 공지가 등록되었습니다.", teamName, noticeTitle);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "notice");
        data.put("teamName", teamName);
        data.put("noticeTitle", noticeTitle);
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    /**
     * 팀원 입장/나가기 알림 전송
     */
    public void sendTeamMemberNotification(List<String> expoPushTokens, String memberName, String teamName, boolean isJoin) {
        String title = "팀원 알림";
        String body = isJoin 
            ? String.format("[%s] %s님이 팀에 입장했습니다.", teamName, memberName)
            : String.format("[%s] %s님이 팀에서 나갔습니다.", teamName, memberName);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "team_member");
        data.put("teamName", teamName);
        data.put("memberName", memberName);
        data.put("action", isJoin ? "join" : "leave");
        
        sendNotificationToUser(expoPushTokens, title, body, data);
    }

    // Expo Push API 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpoPushResponse {
        private List<ExpoPushTicket> data;
        private List<ExpoPushError> errors; // 전체 요청 레벨 에러
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpoPushError {
        private String code;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpoPushTicket {
        private String status;
        private String id;
        private String message;
        private Map<String, Object> details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpoPushMessage {
        @JsonProperty("to")
        private String to;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("body")
        private String body;
        
        @JsonProperty("data")
        private Map<String, String> data;
        
        @JsonProperty("sound")
        private String sound;
        
        @JsonProperty("priority")
        private String priority;
    }
}










