package com.example.demo.repository;

import com.example.demo.domain.entity.Position;
import com.example.demo.dto.team.PositionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByTeamId(Long teamId);
    
    @Query("SELECT COUNT(p) FROM Position p WHERE p.team.id = :teamId")
    long countByTeamId(@Param("teamId") Long teamId);
    
    Optional<Position> findByTeamIdAndName(Long teamId, String name);
    
    @Query("""
            select 
                p.id as positionId,
                p.team.id as teamId,
                p.name as positionName,
                p.colorHex as colorHex,
                p.isDefault as isDefault,
                p.createdAt as createdAt,
                p.updatedAt as updatedAt
            from Position p
            where p.team.id = :teamId
            order by p.isDefault desc, p.createdAt
            """)
    List<PositionResponse> findResponsesByTeamId(@Param("teamId") Long teamId);
    
    @Query("""
            select 
                p.id as positionId,
                p.team.id as teamId,
                p.name as positionName,
                p.colorHex as colorHex,
                p.isDefault as isDefault,
                p.createdAt as createdAt,
                p.updatedAt as updatedAt
            from Position p
            where p.id = :positionId
            """)
    Optional<PositionResponse> findResponseById(@Param("positionId") Long positionId);
}
