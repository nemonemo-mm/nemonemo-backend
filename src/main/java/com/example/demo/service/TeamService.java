package com.example.demo.service;

import com.example.demo.domain.entity.Position;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.TeamMember;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.PositionRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
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
import com.example.demo.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * 팀 생성
     */
    @Transactional
    public TeamDetailResponse createTeam(Long userId, TeamCreateRequest request) {
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
        positionRepository.save(defaultPosition);
        
        // 팀장을 팀 멤버로 추가
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(owner)
                .build();
        teamMemberRepository.save(ownerMember);
        
        return toDetailResponse(team, userId);
    }
    
    /**
     * 팀 수정 (부분 수정)
     */
    @Transactional
    public TeamDetailResponse updateTeam(Long userId, Long teamId, TeamUpdateRequest request) {
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
    public TeamDetailResponse getTeamDetail(Long userId, Long teamId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        Team team = teamPermissionService.getTeamWithMemberCheck(userId, teamId);
        
        return toDetailResponse(team, userId);
    }
    
    /**
     * 팀 목록 조회 (사용자가 속한 팀들)
     */
    @Transactional(readOnly = true)
    public List<TeamDetailResponse> getTeamList(Long userId) {
        List<Team> teams = teamRepository.findByUserId(userId);
        return teams.stream()
                .map(team -> toDetailResponseForList(team, userId))
                .collect(Collectors.toList());
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
     * 초대 코드 조회 (팀장만)
     */
    @Transactional(readOnly = true)
    public InviteCodeResponse getInviteCode(Long userId, Long teamId) {
        // 권한 확인 및 팀 조회 (팀장만 조회 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        return InviteCodeResponse.builder()
                .teamId(teamId)
                .inviteCode(team.getInviteCode())
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
        
        return toMemberResponse(member);
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
        
        // 멤버 삭제
        teamMemberRepository.delete(member);
        
        return TeamLeaveResponse.builder()
                .teamId(teamId)
                .memberId(memberId)
                .userId(memberUserId)
                .build();
    }
    
    /**
     * Team 엔티티를 TeamDetailResponse로 변환 (상세 조회용)
     */
    private TeamDetailResponse toDetailResponse(Team team, Long currentUserId) {
        boolean isOwner = team.getOwner().getId().equals(currentUserId);
        
        return TeamDetailResponse.builder()
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
     * Team 엔티티를 TeamDetailResponse로 변환 (목록 조회용, inviteCode 제외)
     */
    private TeamDetailResponse toDetailResponseForList(Team team, Long currentUserId) {
        boolean isOwner = team.getOwner().getId().equals(currentUserId);
        
        return TeamDetailResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .inviteCode(null)  // 목록 조회에서는 inviteCode 제외
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
        
        // 팀원 목록 조회
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        
        return members.stream()
                .map(this::toMemberListItemResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 팀원 상세 조회
     */
    @Transactional(readOnly = true)
    public TeamMemberResponse getTeamMemberDetail(Long userId, Long teamId, Long memberId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        teamPermissionService.verifyTeamMember(userId, teamId);
        
        // 팀원 조회
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다."));
        
        // 해당 팀의 멤버인지 확인
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다.");
        }
        
        return toMemberResponse(member);
    }
    
    /**
     * 팀원 정보 수정
     */
    @Transactional
    public TeamMemberResponse updateTeamMember(Long userId, Long teamId, Long memberId, TeamMemberUpdateRequest request) {
        // 권한 확인 및 팀 조회 (팀원 모두 접근 가능)
        Team team = teamPermissionService.getTeamWithMemberCheck(userId, teamId);
        
        // 팀원 조회
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다."));
        
        // 해당 팀의 멤버인지 확인
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("TEAM_MEMBER_NOT_FOUND: 팀원을 찾을 수 없습니다.");
        }
        
        // 권한 확인 (본인 정보 수정은 모두 가능, 다른 팀원 정보 수정은 팀장만 가능)
        boolean isOwner = team.getOwner().getId().equals(userId);
        boolean isSelf = member.getUser().getId().equals(userId);
        
        if (!isSelf && !isOwner) {
            throw new IllegalArgumentException("FORBIDDEN: 본인 정보만 수정할 수 있습니다.");
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
        
        return toMemberResponse(member);
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
        
        // 팀원 삭제
        teamMemberRepository.delete(member);
        
        return TeamMemberDeleteResponse.builder()
                .teamId(teamId)
                .memberId(memberId)
                .userId(member.getUser().getId())
                .build();
    }
    
    /**
     * TeamMember 엔티티를 TeamMemberResponse로 변환
     */
    private TeamMemberResponse toMemberResponse(TeamMember member) {
        Position position = member.getPosition();
        boolean isOwner = member.getTeam().getOwner().getId().equals(member.getUser().getId());
        
        return TeamMemberResponse.builder()
                .teamMemberId(member.getId())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .userImageUrl(member.getUser().getImageUrl())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .positionId(position != null ? position.getId() : null)
                .positionName(position != null ? position.getName() : null)
                .positionColor(position != null ? position.getColorHex() : null)
                .isOwner(isOwner)
                .joinedAt(member.getJoinedAt())
                .build();
    }
    
    /**
     * TeamMember 엔티티를 TeamMemberListItemResponse로 변환
     */
    private TeamMemberListItemResponse toMemberListItemResponse(TeamMember member) {
        // displayName: 사용자 이름 사용
        String displayName = member.getUser().getName();
        
        Position position = member.getPosition();
        
        return TeamMemberListItemResponse.builder()
                .teamMemberId(member.getId())
                .userId(member.getUser().getId())
                .displayName(displayName)
                .positionId(position != null ? position.getId() : null)
                .positionName(position != null ? position.getName() : null)
                .userImageUrl(member.getUser().getImageUrl())
                .build();
    }
}
