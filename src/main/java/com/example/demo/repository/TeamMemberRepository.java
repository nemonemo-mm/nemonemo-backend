package com.example.demo.repository;

import com.example.demo.domain.entity.TeamMember;
import com.example.demo.dto.team.TeamMemberListItemResponse;
import com.example.demo.dto.team.TeamMemberResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
    
    List<TeamMember> findByTeamId(Long teamId);
    
    List<TeamMember> findByUserId(Long userId);
    
    // 특정 팀에서 주어진 포지션들에 속한 팀원들 조회
    List<TeamMember> findByTeamIdAndPositionIdIn(Long teamId, List<Long> positionIds);
    
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    
    @Query("""
            select 
                tm.id as memberId,
                tm.team.id as teamId,
                tm.team.name as teamName,
                tm.user.imageUrl as userImageUrl,
                tm.user.id as userId,
                tm.user.name as userName,
                tm.user.email as userEmail,
                tm.position.id as positionId,
                tm.position.name as positionName,
                tm.position.colorHex as positionColor,
                case when tm.team.owner.id = tm.user.id then true else false end as isOwner,
                tm.joinedAt as joinedAt
            from TeamMember tm
            where tm.id = :memberId
            """)
    Optional<TeamMemberResponse> findResponseById(@Param("memberId") Long memberId);
    
    @Query("""
            select 
                tm.id as memberId,
                tm.user.id as userId,
                tm.user.name as userName,
                coalesce(p.id, -1) as positionId,
                coalesce(p.name, 'MEMBER') as positionName,
                coalesce(p.colorHex, '#9BBF9B') as positionColor,
                tm.user.imageUrl as userImageUrl,
                case when tm.team.owner.id = tm.user.id then true else false end as isOwner
            from TeamMember tm
            left join tm.position p
            where tm.team.id = :teamId
            order by case when tm.team.owner.id = tm.user.id then 0 else 1 end, tm.joinedAt
            """)
    List<TeamMemberListItemResponse> findListItemResponsesByTeamId(@Param("teamId") Long teamId);
}
