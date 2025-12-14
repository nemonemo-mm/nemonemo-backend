package com.example.demo.repository;

import com.example.demo.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByInviteCode(String inviteCode);
    
    @Query("SELECT t FROM Team t WHERE t.owner.id = :ownerId")
    List<Team> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT t FROM Team t JOIN TeamMember tm ON tm.team.id = t.id WHERE tm.user.id = :userId")
    List<Team> findByUserId(@Param("userId") Long userId);
}
