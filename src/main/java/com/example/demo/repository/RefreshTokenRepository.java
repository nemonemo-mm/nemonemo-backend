package com.example.demo.repository;

import com.example.demo.domain.entity.RefreshToken;
import com.example.demo.domain.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    /**
     * 토큰으로 RefreshToken을 조회하며 Pessimistic Lock을 걸어 동시 요청을 방지합니다.
     * Refresh Token 로테이션 시 사용됩니다.
     * 
     * @param token 리프레시 토큰 문자열
     * @return RefreshToken (락이 걸린 상태)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithLock(@Param("token") String token);

    void deleteByUserAndToken(User user, String token);
}
