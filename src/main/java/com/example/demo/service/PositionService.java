package com.example.demo.service;

import com.example.demo.domain.entity.Position;
import com.example.demo.repository.PositionRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.dto.team.PositionCreateRequest;
import com.example.demo.dto.team.PositionDeleteResponse;
import com.example.demo.dto.team.PositionResponse;
import com.example.demo.dto.team.PositionUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionService {
    
    private final PositionRepository positionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamPermissionService teamPermissionService;
    
    /**
     * 포지션 목록 조회
     */
    @Transactional
    public List<PositionResponse> getPositionList(Long userId, Long teamId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        teamPermissionService.getTeamWithMemberCheck(userId, teamId);
        
        // 포지션 목록 조회 (인터페이스 프로젝션 사용)
        return positionRepository.findResponsesByTeamId(teamId);
    }
    
    /**
     * 포지션 생성
     */
    @Transactional
    public PositionResponse createPosition(Long userId, Long teamId, PositionCreateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 생성 가능)
        com.example.demo.domain.entity.Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 포지션 이름 검증
        if (request.getPositionName() == null || request.getPositionName().trim().isEmpty()) {
            throw new IllegalArgumentException("포지션 이름은 필수입니다.");
        }
        if (request.getPositionName().length() > 10) {
            throw new IllegalArgumentException("포지션 이름은 최대 10자까지 입력 가능합니다.");
        }
        
        // 중복 이름 체크
        if (positionRepository.findByTeamIdAndName(teamId, request.getPositionName().trim()).isPresent()) {
            throw new IllegalArgumentException("DUPLICATE_POSITION_NAME: 이미 존재하는 포지션 이름입니다.");
        }
        
        // 포지션 개수 체크 (최대 8개까지 추가 가능)
        long positionCount = positionRepository.countByTeamId(teamId);
        if (positionCount >= 8) {
            throw new IllegalArgumentException("포지션은 최대 8개까지 추가할 수 있습니다.");
        }
        
        // 포지션 생성
        Position position = Position.builder()
                .team(team)
                .name(request.getPositionName().trim())
                .colorHex(request.getColorHex())
                .isDefault(false) // 생성 시에는 항상 false
                .build();
        
        position = positionRepository.save(position);
        
        return positionRepository.findResponseById(position.getId())
                .orElseThrow(() -> new IllegalStateException("포지션 저장 후 조회 실패"));
    }
    
    /**
     * 포지션 수정
     */
    @Transactional
    public PositionResponse updatePosition(Long userId, Long teamId, Long positionId, PositionUpdateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 수정 가능)
        teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 포지션 조회
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다."));
        
        // 해당 팀의 포지션인지 확인
        if (!position.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다.");
        }
        
        // 이름 수정
        if (request.getPositionName() != null) {
            if (request.getPositionName().trim().isEmpty()) {
                throw new IllegalArgumentException("포지션 이름은 비어있을 수 없습니다.");
            }
            if (request.getPositionName().length() > 10) {
                throw new IllegalArgumentException("포지션 이름은 최대 10자까지 입력 가능합니다.");
            }
            
            // 중복 이름 체크 (현재 이름과 다를 때만)
            if (!request.getPositionName().trim().equals(position.getName())) {
                if (positionRepository.findByTeamIdAndName(teamId, request.getPositionName().trim()).isPresent()) {
                    throw new IllegalArgumentException("DUPLICATE_POSITION_NAME: 이미 존재하는 포지션 이름입니다.");
                }
            }
            
            position.setName(request.getPositionName().trim());
        }
        
        // 색상 수정
        if (request.getColorHex() != null) {
            if (request.getColorHex().length() > 9) {
                throw new IllegalArgumentException("색상 코드는 최대 9자까지 입력 가능합니다.");
            }
            position.setColorHex(request.getColorHex());
        }
        
        position = positionRepository.save(position);
        
        return positionRepository.findResponseById(position.getId())
                .orElseThrow(() -> new IllegalStateException("포지션 저장 후 조회 실패"));
    }
    
    /**
     * 포지션 삭제
     */
    @Transactional
    public PositionDeleteResponse deletePosition(Long userId, Long teamId, Long positionId) {
        // 권한 확인 (팀장만 삭제 가능)
        teamPermissionService.verifyTeamOwner(userId, teamId);
        
        // 포지션 조회
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다."));
        
        // 해당 팀의 포지션인지 확인
        if (!position.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다.");
        }
        
        // 해당 포지션을 사용하는 팀 멤버들의 포지션을 null로 변경 (전체로 처리)
        teamMemberRepository.findByTeamId(teamId).stream()
                .filter(member -> member.getPosition() != null && member.getPosition().getId().equals(positionId))
                .forEach(member -> member.setPosition(null));
        
        // 포지션 삭제
        positionRepository.delete(position);
        
        return PositionDeleteResponse.builder()
                .teamId(teamId)
                .positionId(positionId)
                .build();
    }
    
}
