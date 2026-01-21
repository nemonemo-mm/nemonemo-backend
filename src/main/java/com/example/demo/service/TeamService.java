package com.example.demo.service;

import com.example.demo.domain.entity.NotificationSetting;
import com.example.demo.domain.entity.Position;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.TeamMember;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.PositionRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.team.PositionResponse;
import com.example.demo.dto.team.TeamCreateRequest;
import com.example.demo.dto.team.TeamDeleteResponse;
import com.example.demo.dto.team.TeamDetailResponse;
import com.example.demo.dto.team.TeamDetailResponseDto;
import com.example.demo.dto.team.TeamInvitePreviewResponse;
import com.example.demo.dto.team.TeamJoinRequest;
import com.example.demo.dto.team.TeamLeaveResponse;
import com.example.demo.dto.team.TeamMemberDeleteResponse;
import com.example.demo.dto.team.TeamMemberListItemResponse;
import com.example.demo.dto.team.TeamMemberResponse;
import com.example.demo.dto.team.TeamMemberUpdateRequest;
import com.example.demo.dto.team.TeamUpdateRequest;
import com.example.demo.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final TeamPermissionService teamPermissionService;
    private final FirebaseStorageService firebaseStorageService;
    private final ExpoNotificationService expoNotificationService;
    private final DeviceTokenService deviceTokenService;
    private final NotificationSettingRepository notificationSettingRepository;
    
    /**
     * 팀 생성
     */
    @Transactional
    public TeamDetailResponseDto createTeam(Long userId, TeamCreateRequest request) {
        // 사용자 조회
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 팀 이름 검증
        if (request.getTeamName() == null || request.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다.");
        }
        if (request.getTeamName().length() > 10) {
            throw new IllegalArgumentException("팀 이름은 최대 10자까지 입력 가능합니다.");
        }
        
        // 초대 코드 생성
        String inviteCode = inviteCodeGenerator.generateUniqueInviteCode();
        
        // 팀 생성
        Team team = Team.builder()
                .name(request.getTeamName().trim())
                .inviteCode(inviteCode)
                .owner(owner)
                .imageUrl(null)
                .description(request.getDescription())
                .build();
        
        team = teamRepository.save(team);
        
        // 기본 포지션(MEMBER) 생성
        Position defaultPosition = Position.builder()
                .team(team)
                .name("MEMBER")
                .isDefault(true)
                .build();
        defaultPosition = positionRepository.save(defaultPosition);
        
        // 요청에 포함된 포지션들 생성
        if (request.getPositions() != null && !request.getPositions().isEmpty()) {
            // 포지션 개수 체크 (기본 MEMBER 포함 최대 7개)
            int totalPositionCount = 1 + request.getPositions().size(); // MEMBER + 요청된 포지션들
            if (totalPositionCount > 7) {
                throw new IllegalArgumentException("포지션은 최대 6개까지 추가할 수 있습니다. (기본값 MEMBER 포함 시 7개)");
            }
            
            // 포지션 이름 중복 체크
            List<String> positionNames = request.getPositions().stream()
                    .map(p -> p.getPositionName() != null ? p.getPositionName().trim() : null)
                    .filter(name -> name != null && !name.isEmpty())
                    .toList();
            
            // MEMBER와 중복 체크
            if (positionNames.stream().anyMatch(name -> "MEMBER".equalsIgnoreCase(name))) {
                throw new IllegalArgumentException("MEMBER는 기본 포지션이므로 추가할 수 없습니다.");
            }
            
            // 포지션 이름 중복 체크
            long distinctCount = positionNames.stream().distinct().count();
            if (distinctCount != positionNames.size()) {
                throw new IllegalArgumentException("중복된 포지션 이름이 있습니다.");
            }
            
            // 포지션 생성
            for (com.example.demo.dto.team.PositionCreateRequest positionRequest : request.getPositions()) {
                String positionName = positionRequest.getPositionName();
                if (positionName == null || positionName.trim().isEmpty()) {
                    continue; // 빈 이름은 건너뛰기
                }
                
                positionName = positionName.trim();
                
                // 이미 존재하는지 확인 (MEMBER 제외하고는 아직 생성 안 했으므로 체크 불필요하지만 안전을 위해)
                if (positionRepository.findByTeamIdAndName(team.getId(), positionName).isPresent()) {
                    throw new IllegalArgumentException("이미 존재하는 포지션 이름입니다: " + positionName);
                }
                
                Position position = Position.builder()
                        .team(team)
                        .name(positionName)
                        .colorHex(positionRequest.getColorHex())
                        .isDefault(false)
                        .build();
                positionRepository.save(position);
            }
        }
        
        // 팀장의 포지션 설정
        Position ownerPosition = defaultPosition;
        if (request.getOwnerPositionName() != null && !request.getOwnerPositionName().trim().isEmpty()) {
            String ownerPositionName = request.getOwnerPositionName().trim();
            Position selectedPosition = positionRepository.findByTeamIdAndName(team.getId(), ownerPositionName)
                    .orElseThrow(() -> new IllegalArgumentException("포지션을 찾을 수 없습니다: " + ownerPositionName));
            ownerPosition = selectedPosition;
        }
        
        // 팀장을 팀 멤버로 추가
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(owner)
                .position(ownerPosition)
                .build();
        teamMemberRepository.save(ownerMember);
        
        // 팀장의 팀 알림 설정 생성
        if (!notificationSettingRepository.findByUserIdAndTeamId(userId, team.getId()).isPresent()) {
            NotificationSetting ownerNotificationSetting = NotificationSetting.builder()
                    .user(owner)
                    .team(team)
                    .build();
            notificationSettingRepository.save(ownerNotificationSetting);
        }
        
        return toDetailResponse(team, userId);
    }
    
    /**
     * 팀 수정 (부분 수정)
     */
    @Transactional
    public TeamDetailResponseDto updateTeam(Long userId, Long teamId, TeamUpdateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 수정 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 부분 수정
        if (request.getTeamName() != null) {
            if (request.getTeamName().trim().isEmpty()) {
                throw new IllegalArgumentException("팀 이름은 비어있을 수 없습니다.");
            }
            if (request.getTeamName().length() > 10) {
                throw new IllegalArgumentException("팀 이름은 최대 10자까지 입력 가능합니다.");
            }
            team.setName(request.getTeamName().trim());
        }

        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }
        
        // 이미지 URL 삭제 처리
        // 프론트엔드에서 이미지 삭제 시 "deleteImageUrl": true를 보내면 이미지가 삭제됩니다.
        if (Boolean.TRUE.equals(request.getDeleteImageUrl())) {
            String oldImageUrl = team.getImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                // Firebase Storage에서 기존 이미지 삭제
                firebaseStorageService.deleteImage(oldImageUrl);
            }
            team.setImageUrl(null);
        }
        // deleteImageUrl 필드가 요청에 포함되지 않았거나 false인 경우는 변경하지 않음
        
        team = teamRepository.save(team);
        
        return toDetailResponse(team, userId);
    }
    
    /**
     * 팀 상세 조회
     */
    @Transactional(readOnly = true)
    public TeamDetailResponseDto getTeamDetail(Long userId, Long teamId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        Team team = teamPermissionService.getTeamWithMemberCheck(userId, teamId);
        
        return toDetailResponse(team, userId);
    }
    
    /**
     * 팀 목록 조회 (사용자가 속한 팀들)
     */
    @Transactional(readOnly = true)
    public List<TeamDetailResponseDto> getTeamList(Long userId) {
        List<TeamDetailResponse> responses = teamRepository.findByUserId(userId);
        return responses.stream()
                .map(response -> enrichTeamDetailResponse(response, userId))
                .collect(Collectors.toList());
    }
    
    private TeamDetailResponseDto enrichTeamDetailResponse(TeamDetailResponse response, Long userId) {
        Team team = teamRepository.findById(response.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        
        boolean isOwner = team.getOwner().getId().equals(userId);
        String inviteCode = isOwner ? team.getInviteCode() : null;
        
        return TeamDetailResponseDto.builder()
                .teamId(response.getTeamId())
                .teamName(response.getTeamName())
                .inviteCode(inviteCode)
                .ownerId(response.getOwnerId())
                .ownerName(response.getOwnerName())
                .isOwner(isOwner)
                .description(response.getDescription())
                .teamImageUrl(response.getTeamImageUrl())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
    
    /**
     * 팀 삭제
     */
    @Transactional
    public TeamDeleteResponse deleteTeam(Long userId, Long teamId) {
        // 권한 확인 (팀장만 삭제 가능)
        teamPermissionService.verifyTeamOwner(userId, teamId);
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        teamRepository.delete(team);
        
        return TeamDeleteResponse.builder()
                .teamId(teamId)
                .build();
    }
    
    /**
     * 인바이트 코드로 팀 정보 조회 (가입 전 미리보기)
     */
    @Transactional(readOnly = true)
    public TeamInvitePreviewResponse getTeamByInviteCode(String inviteCode) {
        // 초대 코드로 팀 조회
        Team team = teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_INVITE_CODE: 유효하지 않은 초대 코드입니다."));
        
        // 포지션 목록 조회
        List<PositionResponse> positions = positionRepository.findResponsesByTeamId(team.getId());
        
        // 팀 정보 + 포지션 목록 반환
        return TeamInvitePreviewResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .ownerName(team.getOwner().getName())
                .description(team.getDescription())
                .teamImageUrl(team.getImageUrl())
                .positions(positions)
                .build();
    }
    
    /**
     * 팀 참여 (팀원만, 팀장 불가)
     */
    @Transactional
    public TeamMemberResponse joinTeam(Long userId, TeamJoinRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 초대 코드로 팀 조회
        Team team = teamRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_INVITE_CODE: 유효하지 않은 초대 코드입니다."));
        
        // 팀장인지 확인 (팀장은 참여 불가)
        if (team.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("팀장은 팀에 참여할 수 없습니다.");
        }
        
        // 이미 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId)) {
            throw new IllegalArgumentException("ALREADY_MEMBER: 이미 해당 팀의 멤버입니다.");
        }
        
        // 포지션 설정
        Position position = null;
        if (request.getPositionId() != null) {
            position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new IllegalArgumentException("포지션을 찾을 수 없습니다."));
            
            // 해당 포지션이 이 팀의 포지션인지 확인
            if (!position.getTeam().getId().equals(team.getId())) {
                throw new IllegalArgumentException("해당 팀의 포지션이 아닙니다.");
            }
        } else {
            // 기본 포지션(MEMBER) 찾기
            position = positionRepository.findByTeamIdAndName(team.getId(), "MEMBER")
                    .orElseThrow(() -> new IllegalArgumentException("기본 포지션을 찾을 수 없습니다."));
        }
        
        // 팀 멤버 생성
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .position(position)
                .build();
        
        member = teamMemberRepository.save(member);
        
        // 팀 알림 설정 생성 (없는 경우에만)
        if (!notificationSettingRepository.findByUserIdAndTeamId(userId, team.getId()).isPresent()) {
            NotificationSetting notificationSetting = NotificationSetting.builder()
                    .user(user)
                    .team(team)
                    .build();
            notificationSettingRepository.save(notificationSetting);
        }
        
        // 팀원 정보 조회
        TeamMemberResponse memberResponse = teamMemberRepository.findResponseById(member.getId())
                .orElseThrow(() -> new IllegalStateException("팀원 저장 후 조회 실패"));
        
        // 팀 참여 FCM 알림 전송 (참여한 사용자 제외)
        sendTeamMemberNotification(team, user.getName(), userId, true);
        
        return memberResponse;
    }
    
    /**
     * 팀 탈퇴 (팀원만, 팀장 불가)
     */
    @Transactional
    public TeamLeaveResponse leaveTeam(Long userId, Long teamId) {
        // 팀장인지 확인 (팀장은 탈퇴 불가)
        teamPermissionService.verifyNotTeamOwner(userId, teamId);
        
        // 멤버 조회
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_A_MEMBER: 해당 팀의 멤버가 아닙니다."));
        
        // 멤버 ID와 사용자 ID 저장 (삭제 전에)
        Long memberId = member.getId();
        Long memberUserId = member.getUser().getId();
        String memberName = member.getUser().getName();
        Team team = member.getTeam();
        
        // 멤버 삭제
        teamMemberRepository.delete(member);
        
        // 멤버 퇴장 FCM 알림 전송 (퇴장한 사용자 제외)
        sendTeamMemberNotification(team, memberName, memberUserId, false);
        
        return TeamLeaveResponse.builder()
                .teamId(teamId)
                .memberId(memberId)
                .userId(memberUserId)
                .build();
    }
    
    /**
     * Team 엔티티를 TeamDetailResponseDto로 변환 (상세 조회용)
     */
    private TeamDetailResponseDto toDetailResponse(Team team, Long currentUserId) {
        boolean isOwner = team.getOwner().getId().equals(currentUserId);
        
        return TeamDetailResponseDto.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .inviteCode(isOwner ? team.getInviteCode() : null)
                .ownerId(team.getOwner().getId())
                .ownerName(team.getOwner().getName())
                .isOwner(isOwner)
                .description(team.getDescription())
                .teamImageUrl(team.getImageUrl())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
    
    /**
     * 팀원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TeamMemberListItemResponse> getTeamMemberList(Long userId, Long teamId) {
        // 팀 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        
        // 권한 확인 (팀원인지 확인)
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
        if (!isMember && !team.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 팀의 멤버만 조회할 수 있습니다.");
        }
        
        // 팀원 목록 조회 (인터페이스 프로젝션 사용)
        return teamMemberRepository.findListItemResponsesByTeamId(teamId);
    }
    
    /**
     * 팀원 상세 조회
     */
    @Transactional(readOnly = true)
    public TeamMemberResponse getTeamMemberDetail(Long userId, Long teamId, Long memberId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        teamPermissionService.verifyTeamMember(userId, teamId);
        
        // 팀원 조회 (인터페이스 프로젝션 사용)
        TeamMemberResponse response = teamMemberRepository.findResponseById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다."));
        
        // 해당 팀의 멤버인지 확인
        if (!response.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다.");
        }
        
        return response;
    }
    
    /**
     * 팀원 정보 수정
     */
    @Transactional
    public TeamMemberResponse updateTeamMember(Long userId, Long teamId, Long memberId, TeamMemberUpdateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 접근 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 팀원 조회
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다."));
        
        // 해당 팀의 멤버인지 확인
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다.");
        }
        
        // 권한 확인 (팀장만 수정 가능)
        boolean isOwner = team.getOwner().getId().equals(userId);
        if (!isOwner) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장만 수정할 수 있습니다.");
        }
        
        // 포지션 수정
        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new IllegalArgumentException("INVALID_ROLE_CATEGORY: 유효하지 않은 포지션입니다."));
            
            // 해당 팀의 포지션인지 확인
            if (!position.getTeam().getId().equals(teamId)) {
                throw new IllegalArgumentException("INVALID_ROLE_CATEGORY: 유효하지 않은 포지션입니다.");
            }
            
            member.setPosition(position);
        }
        
        member = teamMemberRepository.save(member);
        
        return teamMemberRepository.findResponseById(member.getId())
                .orElseThrow(() -> new IllegalStateException("팀원 저장 후 조회 실패"));
    }
    
    /**
     * 팀원 삭제 (팀장만 가능)
     */
    @Transactional
    public TeamMemberDeleteResponse deleteTeamMember(Long userId, Long teamId, Long memberId) {
        // 권한 확인 및 팀 조회 (팀장만 삭제 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 팀원 조회
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다."));
        
        // 해당 팀의 멤버인지 확인
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다.");
        }
        
        // 팀장은 삭제할 수 없음
        if (member.getUser().getId().equals(team.getOwner().getId())) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장은 삭제할 수 없습니다.");
        }
        
        // 삭제 전에 멤버 정보 조회
        Long memberUserId = member.getUser().getId();
        String memberName = member.getUser().getName();
        
        // 팀원 삭제
        teamMemberRepository.delete(member);
        
        // 팀원 삭제 FCM 알림 전송 (삭제된 사용자 제외)
        sendTeamMemberNotification(team, memberName, memberUserId, false);
        
        return TeamMemberDeleteResponse.builder()
                .teamId(teamId)
                .memberId(memberId)
                .userId(memberUserId)
                .build();
    }
    
    /**
     * 팀 멤버 입장/퇴장 FCM 알림 전송
     * @param team 팀
     * @param memberName 멤버 이름
     * @param excludeUserId 알림을 보내지 않을 사용자 ID (입장/퇴장한 사용자)
     * @param isJoin 입장 여부 (true: 입장, false: 퇴장)
     */
    private void sendTeamMemberNotification(Team team, String memberName, Long excludeUserId, boolean isJoin) {
        Long teamId = team.getId();
        String teamName = team.getName();
        
        // 팀의 모든 멤버 조회
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(teamId);
        if (teamMembers == null || teamMembers.isEmpty()) {
            return;
        }
        
        // 알림을 받을 사용자 목록 수집
        List<String> deviceTokens = new ArrayList<>();
        for (TeamMember teamMember : teamMembers) {
            Long userId = teamMember.getUser().getId();
            
            // 입장/퇴장한 사용자 제외
            if (userId.equals(excludeUserId)) {
                continue;
            }
            
            // 알림 설정 확인
            boolean shouldNotify = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId)
                    .map(setting -> Boolean.TRUE.equals(setting.getEnableTeamAlarm()) &&
                            Boolean.TRUE.equals(setting.getEnableTeamMemberNotification()))
                    .orElse(true); // 설정이 없으면 기본값으로 알림 전송
            
            if (shouldNotify) {
                deviceTokenService.getDeviceTokenByUserId(userId)
                        .ifPresent(deviceTokens::add);
            }
        }
        
        if (!deviceTokens.isEmpty()) {
            expoNotificationService.sendTeamMemberNotification(deviceTokens, memberName, teamName, isJoin);
        }
    }
    
}
