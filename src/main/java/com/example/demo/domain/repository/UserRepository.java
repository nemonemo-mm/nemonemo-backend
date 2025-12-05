package com.example.demo.domain.repository;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM app_user WHERE provider::text = :provider AND provider_id = :providerId", nativeQuery = true)
    Optional<User> findByProviderAndProviderIdNative(@Param("provider") String provider, @Param("providerId") String providerId);

    default Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return findByProviderAndProviderIdNative(provider.name(), providerId);
    }
}


