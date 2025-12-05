package com.example.demo.domain.repository;

import com.example.demo.domain.entity.RefreshToken;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUserAndToken(User user, String token);
}


