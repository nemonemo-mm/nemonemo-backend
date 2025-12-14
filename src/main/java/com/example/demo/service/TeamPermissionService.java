package com.example.demo.service;

import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.TeamMember;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamPermissionService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * 팀장인지 확인 (팀장만 가능한 작업용)
     * @throws IllegalArgumentException 팀장이 아닌 경우
     */
    public void verifyTeamOwner(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        if (!team.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장 권한이 필요합니다.");
        }
    }
    
    /**
     * 팀원인지 확인 (팀원 모두 가능한 작업용)
     * @throws IllegalArgumentException 팀원이 아닌 경우
     */
    public void verifyTeamMember(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
        boolean isOwner = team.getOwner().getId().equals(userId);
        
        if (!isMember && !isOwner) {
            throw new IllegalArgumentException("FORBIDDEN: 해당 팀의 멤버만 접근할 수 있습니다.");
        }
    }
    
    /**
     * 팀장인지 확인 (팀원만 가능, 팀장 불가능한 작업용)
     * @throws IllegalArgumentException 팀장인 경우
     */
    public void verifyNotTeamOwner(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        if (team.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장은 이 작업을 수행할 수 없습니다.");
        }
    }
    
    /**
     * 팀 조회 및 권한 확인
     * @return Team 엔티티
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    public Team getTeamWithMemberCheck(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
        boolean isOwner = team.getOwner().getId().equals(userId);
        
        if (!isMember && !isOwner) {
            throw new IllegalArgumentException("FORBIDDEN: 해당 팀의 멤버만 접근할 수 있습니다.");
        }
        
        return team;
    }
    
    /**
     * 팀 조회 및 팀장 확인
     * @return Team 엔티티
     * @throws IllegalArgumentException 팀장이 아닌 경우
     */
    public Team getTeamWithOwnerCheck(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));
        
        if (!team.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀장 권한이 필요합니다.");
        }
        
        return team;
    }
}
