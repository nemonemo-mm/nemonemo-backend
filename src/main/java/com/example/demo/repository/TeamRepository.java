package com.example.demo.repository;

import com.example.demo.domain.entity.Team;
import com.example.demo.dto.team.TeamDetailResponse;
import com.example.demo.dto.team.TeamListItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByInviteCode(String inviteCode);
    
    @Query("""
            select 
                t.id as teamId,
                t.name as teamName,
                t.owner.id as ownerId,
                t.owner.name as ownerName,
                t.description as description,
                t.imageUrl as teamImageUrl,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt
            from Team t
            where t.owner.id = :ownerId
            """)
    List<TeamDetailResponse> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("""
            select distinct
                t.id as teamId,
                t.name as teamName,
                t.owner.id as ownerId,
                t.owner.name as ownerName,
                t.description as description,
                t.imageUrl as teamImageUrl,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt
            from Team t
            join TeamMember tm on tm.team.id = t.id
            where tm.user.id = :userId
            """)
    List<TeamDetailResponse> findByUserId(@Param("userId") Long userId);
    
    @Query("""
            select distinct
                t.id as teamId,
                t.name as teamName,
                p.name as description,
                t.imageUrl as teamImageUrl
            from Team t
            left join TeamMember tm
                on tm.team.id = t.id
                and tm.user.id = :userId
            left join tm.position p
                on p.id = tm.position.id
            where t.owner.id = :userId
               or tm.user.id = :userId
            order by t.id
            """)
    List<TeamListItemResponse> findListItemResponsesByUserId(@Param("userId") Long userId);
}
