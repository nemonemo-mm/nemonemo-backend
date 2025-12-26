package com.example.demo.service;

import com.example.demo.domain.entity.Notice;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notice.NoticeCreateRequest;
import com.example.demo.dto.notice.NoticeResponse;
import com.example.demo.dto.notice.NoticeUpdateRequest;
import com.example.demo.repository.NoticeRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NoticeNotificationHelper noticeNotificationHelper;
    private final TeamPermissionService teamPermissionService;

    @Transactional
    public NoticeResponse createNotice(Long userId, Long teamId, NoticeCreateRequest request) {
        // 팀 조회 및 팀원 확인
        Team team = teamPermissionService.getTeamWithMemberCheck(userId, teamId);

        // 작성자 조회
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 공지 생성
        Notice notice = Notice.builder()
                .team(team)
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .build();

        notice = noticeRepository.save(notice);

        // 공지 생성 알림 전송 (작성자 제외)
        try {
            noticeNotificationHelper.sendNoticeNotification(notice, userId);
        } catch (Exception e) {
            // 알림 전송 실패해도 공지 생성은 성공 처리
            log.warn("공지 알림 전송 실패: noticeId={}, error={}", notice.getId(), e.getMessage());
        }

        return toResponse(notice);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> getTeamNotices(Long userId, Long teamId) {
        // 팀 조회 및 팀원 확인
        teamPermissionService.verifyTeamMember(userId, teamId);

        // 공지 목록 조회
        return noticeRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long userId, Long teamId, Long noticeId) {
        // 팀 조회 및 팀원 확인
        teamPermissionService.verifyTeamMember(userId, teamId);

        // 공지 조회 및 팀 소속 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("NOTICE_NOT_FOUND: 공지를 찾을 수 없습니다."));

        if (!notice.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("NOTICE_NOT_FOUND: 해당 팀의 공지가 아닙니다.");
        }

        return noticeRepository.findNoticeResponseById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("NOTICE_NOT_FOUND: 공지를 찾을 수 없습니다."));
    }

    @Transactional
    public NoticeResponse updateNotice(Long userId, Long teamId, Long noticeId, NoticeUpdateRequest request) {
        // 팀 조회 및 팀원 확인
        teamPermissionService.verifyTeamMember(userId, teamId);

        // 공지 조회 및 팀 소속 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("NOTICE_NOT_FOUND: 공지를 찾을 수 없습니다."));

        if (!notice.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("NOTICE_NOT_FOUND: 해당 팀의 공지가 아닙니다.");
        }

        // 작성자 확인
        if (!notice.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 공지 작성자만 수정할 수 있습니다.");
        }

        // 공지 수정
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice = noticeRepository.save(notice);

        return toResponse(notice);
    }

    @Transactional
    public void deleteNotice(Long userId, Long teamId, Long noticeId) {
        // 팀 조회 및 팀원 확인
        teamPermissionService.verifyTeamMember(userId, teamId);

        // 공지 조회 및 팀 소속 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("NOTICE_NOT_FOUND: 공지를 찾을 수 없습니다."));

        if (!notice.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("NOTICE_NOT_FOUND: 해당 팀의 공지가 아닙니다.");
        }

        // 작성자 확인
        if (!notice.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 공지 작성자만 삭제할 수 있습니다.");
        }

        // 공지 삭제
        noticeRepository.delete(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .teamId(notice.getTeam().getId())
                .teamName(notice.getTeam().getName())
                .title(notice.getTitle())
                .content(notice.getContent())
                .authorId(notice.getAuthor().getId())
                .authorName(notice.getAuthor().getName())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}

