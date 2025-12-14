package com.example.demo.service;

import com.example.demo.domain.entity.Position;
import com.example.demo.domain.entity.Team;
import com.example.demo.repository.PositionRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.dto.team.PositionCreateRequest;
import com.example.demo.dto.team.PositionDeleteResponse;
import com.example.demo.dto.team.PositionResponse;
import com.example.demo.dto.team.PositionUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionService {
    
    private final TeamRepository teamRepository;
    private final PositionRepository positionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamPermissionService teamPermissionService;
    
    /**
     * 포지션 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PositionResponse> getPositionList(Long userId, Long teamId) {
        // 권한 확인 및 팀 조회 (팀원 모두 조회 가능)
        teamPermissionService.verifyTeamMember(userId, teamId);
        
        // 포지션 목록 조회
        List<Position> positions = positionRepository.findByTeamId(teamId);
        
        return positions.stream()
                .map(this::toPositionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 포지션 생성
     */
    @Transactional
    public PositionResponse createPosition(Long userId, Long teamId, PositionCreateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 생성 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 포지션 이름 검증
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("포지션 이름은 필수입니다.");
        }
        if (request.getName().length() > 10) {
            throw new IllegalArgumentException("포지션 이름은 최대 10자까지 입력 가능합니다.");
        }
        
        // 중복 이름 체크
        if (positionRepository.findByTeamIdAndName(teamId, request.getName().trim()).isPresent()) {
            throw new IllegalArgumentException("DUPLICATE_POSITION_NAME: 이미 존재하는 포지션 이름입니다.");
        }
        
        // 포지션 개수 체크 (최대 6개 추가 가능, MEMBER 제외, 총 7개)
        long positionCount = positionRepository.countByTeamId(teamId);
        if (positionCount >= 7) { // MEMBER 포함 7개가 최대
            throw new IllegalArgumentException("포지션은 최대 6개까지 추가할 수 있습니다. (기본값 MEMBER 포함 시 7개)");
        }
        
        // 포지션 생성
        Position position = Position.builder()
                .team(team)
                .name(request.getName().trim())
                .colorHex(request.getColorHex())
                .isDefault(false) // 생성 시에는 항상 false
                .build();
        
        position = positionRepository.save(position);
        
        return toPositionResponse(position);
    }
    
    /**
     * 포지션 수정
     */
    @Transactional
    public PositionResponse updatePosition(Long userId, Long teamId, Long positionId, PositionUpdateRequest request) {
        // 권한 확인 및 팀 조회 (팀장만 수정 가능)
        Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
        
        // 포지션 조회
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다."));
        
        // 해당 팀의 포지션인지 확인
        if (!position.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("POSITION_NOT_FOUND: 포지션을 찾을 수 없습니다.");
        }
        
        // 기본 포지션 이름 변경 불가 체크
        if (request.getName() != null && position.getIsDefault() && position.getName().equals("MEMBER")) {
            throw new IllegalArgumentException("DEFAULT_POSITION_NAME_CANNOT_CHANGE: 기본 포지션의 이름은 변경할 수 없습니다.");
        }
        
        // 이름 수정
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("포지션 이름은 비어있을 수 없습니다.");
            }
            if (request.getName().length() > 10) {
                throw new IllegalArgumentException("포지션 이름은 최대 10자까지 입력 가능합니다.");
            }
            
            // 중복 이름 체크 (현재 이름과 다를 때만)
            if (!request.getName().trim().equals(position.getName())) {
                if (positionRepository.findByTeamIdAndName(teamId, request.getName().trim()).isPresent()) {
                    throw new IllegalArgumentException("DUPLICATE_POSITION_NAME: 이미 존재하는 포지션 이름입니다.");
                }
            }
            
            position.setName(request.getName().trim());
        }
        
        // 색상 수정
        if (request.getColorHex() != null) {
            if (request.getColorHex().length() > 9) {
                throw new IllegalArgumentException("색상 코드는 최대 9자까지 입력 가능합니다.");
            }
            position.setColorHex(request.getColorHex());
        }
        
        position = positionRepository.save(position);
        
        return toPositionResponse(position);
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
        
        // 기본 포지션 삭제 불가
        if (position.getIsDefault()) {
            throw new IllegalArgumentException("DEFAULT_POSITION_CANNOT_DELETE: 기본 포지션은 삭제할 수 없습니다.");
        }
        
        // 포지션 삭제
        positionRepository.delete(position);
        
        return PositionDeleteResponse.builder()
                .teamId(teamId)
                .positionId(positionId)
                .build();
    }
    
    /**
     * Position 엔티티를 PositionResponse로 변환
     */
    private PositionResponse toPositionResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .teamId(position.getTeam().getId())
                .name(position.getName())
                .colorHex(position.getColorHex())
                .isDefault(position.getIsDefault())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
}
