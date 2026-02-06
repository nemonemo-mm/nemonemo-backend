package com.example.demo.service;

import com.example.demo.domain.entity.Alert;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.enums.AlertType;
import com.example.demo.dto.alert.AlertResponseDto;
import com.example.demo.repository.AlertRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public enum AlertScope {
        ALL, GROUP, PERSONAL
    }

    /**
     * 알림 목록 조회 (알림함)
     */
    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAlerts(Long userId, AlertScope scope) {
        List<Alert> alerts;
        if (scope == null || scope == AlertScope.ALL) {
            alerts = alertRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
        } else {
            List<AlertType> types = switch (scope) {
                case GROUP -> List.of(
                        AlertType.SCHEDULE_POSITION_ADDED,
                        AlertType.NOTICE_UPDATED,
                        AlertType.TEAM_MEMBER_JOINED,
                        AlertType.TEAM_DISSOLVED
                );
                case PERSONAL -> List.of(
                        AlertType.SCHEDULE_ASSIGNEE_ADDED,
                        AlertType.TODO_DUE_TODAY
                );
                default -> List.copyOf(EnumSet.allOf(AlertType.class));
            };
            alerts = alertRepository.findTop50ByUserIdAndTypeInOrderByCreatedAtDesc(userId, types);
        }

        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AlertResponseDto toResponse(Alert alert) {
        return AlertResponseDto.builder()
                .id(alert.getId())
                .type(alert.getType())
                .teamId(alert.getTeam() != null ? alert.getTeam().getId() : null)
                .teamName(alert.getTeam() != null ? alert.getTeam().getName() : null)
                .title(alert.getTitle())
                .body(alert.getBody())
                .isRead(alert.getIsRead())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    // ====== 아래 메서드들은 실제 알림 생성용 헬퍼 (나중에 서비스들에서 호출) ======

    @Transactional
    public void createScheduleAssigneeAlert(Long userId, Long teamId, String userName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        String body = String.format("%s님에게 새로운 스케줄이 등록되었습니다.", userName);

        Alert alert = Alert.builder()
                .user(user)
                .team(team)
                .type(AlertType.SCHEDULE_ASSIGNEE_ADDED)
                .title("새로운 스케줄")
                .body(body)
                .build();
        alertRepository.save(alert);
    }

    @Transactional
    public void createSchedulePositionAlert(Long userId, Long teamId, String positionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        String body = String.format("%s 그룹의 새로운 스케줄이 등록되었습니다.", positionName);

        Alert alert = Alert.builder()
                .user(user)
                .team(team)
                .type(AlertType.SCHEDULE_POSITION_ADDED)
                .title("새로운 스케줄")
                .body(body)
                .build();
        alertRepository.save(alert);
    }

    @Transactional
    public void createNoticeUpdatedAlert(Long userId, Long teamId, String teamName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        String body = String.format("%s 팀의 새로운 공지사항을 확인해주세요.", teamName);

        Alert alert = Alert.builder()
                .user(user)
                .team(team)
                .type(AlertType.NOTICE_UPDATED)
                .title("새로운 공지사항")
                .body(body)
                .build();
        alertRepository.save(alert);
    }

    @Transactional
    public void createTodoDueTodayAlert(Long userId, Long teamId, String teamName, String userName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        String body = String.format("%s 팀에서 %s 님의 오늘 할 일이 남아있어요!", teamName, userName);

        Alert alert = Alert.builder()
                .user(user)
                .team(team)
                .type(AlertType.TODO_DUE_TODAY)
                .title("오늘 할 일이 남아있어요")
                .body(body)
                .build();
        alertRepository.save(alert);
    }

    @Transactional
    public void createTeamMemberJoinedAlert(Long userId, Long teamId, String teamName, String newMemberName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        String body = String.format("%s 팀에 새로운 팀원 %s 님이 참여했어요.", teamName, newMemberName);

        Alert alert = Alert.builder()
                .user(user)
                .team(team)
                .type(AlertType.TEAM_MEMBER_JOINED)
                .title("새로운 팀원 참여")
                .body(body)
                .build();
        alertRepository.save(alert);
    }
}


