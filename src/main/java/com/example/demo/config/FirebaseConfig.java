package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key-json-base64:}")
    private String serviceAccountKeyJsonBase64;

    @Value("${firebase.storage-bucket:}")
    private String storageBucket;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder();
                GoogleCredentials credentials = null;

                // Base64 인코딩된 JSON 사용 (환경 변수: FIREBASE_SERVICE_ACCOUNT_KEY_JSON_BASE64)
                if (serviceAccountKeyJsonBase64 != null && !serviceAccountKeyJsonBase64.isEmpty()) {
                    log.info("Using Base64 encoded service account key from environment variable");
                    String decodedJson = new String(
                            Base64.getDecoder().decode(serviceAccountKeyJsonBase64),
                            StandardCharsets.UTF_8
                    );
                    try (InputStream keyStream = new ByteArrayInputStream(decodedJson.getBytes(StandardCharsets.UTF_8))) {
                        credentials = GoogleCredentials.fromStream(keyStream);
                    }
                } else {
                    throw new IllegalStateException("Firebase 인증 정보를 찾을 수 없습니다. 환경 변수 FIREBASE_SERVICE_ACCOUNT_KEY_JSON_BASE64를 설정해주세요.");
                }

                builder.setCredentials(credentials);

                // Storage Bucket 설정
                if (storageBucket != null && !storageBucket.isEmpty()) {
                    builder.setStorageBucket(storageBucket);
                } else {
                    log.warn("Firebase Storage Bucket이 설정되지 않았습니다. 환경 변수 FIREBASE_STORAGE_BUCKET을 설정해주세요.");
                }

                FirebaseOptions options = builder.build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully with bucket: {}", storageBucket);
            } else {
                log.info("Firebase already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
