package com.example.demo.repository;

import com.example.demo.domain.entity.DeviceToken;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByUser(User user);
    
    Optional<DeviceToken> findByUserId(Long userId);
    
    Optional<DeviceToken> findByDeviceToken(String deviceToken);
    
    void deleteByDeviceToken(String deviceToken);
}


