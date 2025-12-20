package com.example.demo.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Firebase ID Token 검증 서비스
 * Firebase Authentication SDK에서 발급받은 ID Token을 검증합니다.
 */
@Slf4j
@Component
public class FirebaseIdTokenVerifierService {

    /**
     * Firebase ID 토큰을 검증하고 FirebaseToken을 반환합니다.
     * 
     * @param idToken 검증할 Firebase ID 토큰
     * @return FirebaseToken 토큰이 유효한 경우
     * @throws FirebaseAuthException 토큰 검증 실패 시
     */
    public FirebaseToken verify(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Firebase ID 토큰이 비어있습니다.");
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken;
        } catch (FirebaseAuthException e) {
            log.error("Firebase ID 토큰 검증 실패: {}", e.getMessage());
            throw e;
        }
    }
}










