package com.example.demo.repository;

import com.example.demo.domain.entity.Position;
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
}
