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
                tm.user.name as displayName,
                tm.position.id as positionId,
                tm.position.name as positionName,
                tm.user.imageUrl as userImageUrl
            from TeamMember tm
            where tm.team.id = :teamId
            order by tm.joinedAt
            """)
    List<TeamMemberListItemResponse> findListItemResponsesByTeamId(@Param("teamId") Long teamId);
}
