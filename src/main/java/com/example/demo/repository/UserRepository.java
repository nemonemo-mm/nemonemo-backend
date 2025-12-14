package com.example.demo.repository;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /**
     * 인증 제공자와 제공자 ID로 사용자 조회
     * PostgreSQL ENUM 타입을 텍스트로 캐스팅하여 비교
     */
    @Query(value = "SELECT * FROM app_user WHERE provider::text = :provider AND provider_id = :providerId", nativeQuery = true)
    Optional<User> findByProviderAndProviderIdNative(@Param("provider") String provider, @Param("providerId") String providerId);

    /**
     * AuthProvider Enum을 사용하는 편의 메서드
     */
    default Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return findByProviderAndProviderIdNative(provider.name(), providerId);
    }
}
