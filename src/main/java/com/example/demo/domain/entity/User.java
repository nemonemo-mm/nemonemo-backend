package com.example.demo.domain.entity;

import com.example.demo.domain.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * 비밀번호 해시 (bcrypt 등)
     * 소셜 로그인만 사용하는 계정은 null
     * 나중에 이메일 기반 로그인/회원가입 추가 시 사용 예정
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 10)
    private String name;

    /**
     * 인증 제공자 (LOCAL, GOOGLE, APPLE)
     * PostgreSQL ENUM 타입(auth_provider)과 매핑
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "auth_provider")
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}







