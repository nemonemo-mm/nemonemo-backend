package com.example.demo.service;

import com.example.demo.domain.enums.ClientType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Google ID Token 검증 서비스
 * Credential Manager API를 포함한 모든 Google Sign-In 방식과 호환됩니다.
 * 
 * Credential Manager API를 사용하는 경우:
 * - Android 앱에서 setServerClientId(WEB_CLIENT_ID)를 사용하므로
 * - 서버에서는 웹 클라이언트 ID도 허용해야 합니다.
 */
@Component
public class GoogleIdTokenVerifierService implements InitializingBean {

    @Value("${auth.google.client-id.ios:}")
    private String iosClientId;

    @Value("${auth.google.client-id.android:}")
    private String androidClientId;

    @Value("${auth.google.client-id.web:}")
    private String webClientId;

    private GoogleIdTokenVerifier verifier;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> clientIds = new ArrayList<>();
        
        // iOS 클라이언트 ID 추가
        if (iosClientId != null && !iosClientId.trim().isEmpty()) {
            clientIds.add(iosClientId);
        }
        
        // Android 클라이언트 ID 추가
        if (androidClientId != null && !androidClientId.trim().isEmpty()) {
            clientIds.add(androidClientId);
        }
        
        // 웹 클라이언트 ID 추가 (Credential Manager API에서 사용)
        // Credential Manager API는 setServerClientId(WEB_CLIENT_ID)를 사용하므로
        // 서버에서도 웹 클라이언트 ID를 허용해야 합니다
        if (webClientId != null && !webClientId.trim().isEmpty()) {
            clientIds.add(webClientId);
        }
        
        if (clientIds.isEmpty()) {
            throw new IllegalStateException("최소 하나의 Google 클라이언트 ID가 필요합니다.");
        }

        // GoogleIdTokenVerifier 생성
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(clientIds)
                .build();
    }

    /**
     * Google ID 토큰을 검증하고 페이로드를 반환합니다.
     * 
     * @param idToken 검증할 ID 토큰
     * @return GoogleIdToken.Payload 토큰이 유효한 경우, null 토큰이 유효하지 않은 경우
     * @throws GeneralSecurityException 토큰 검증 중 보안 오류 발생 시
     * @throws IOException IO 오류 발생 시
     */
    public GoogleIdToken.Payload verify(String idToken) throws GeneralSecurityException, IOException {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("ID 토큰이 비어있습니다.");
        }

        GoogleIdToken token = verifier.verify(idToken);
        return token != null ? token.getPayload() : null;
    }

    /**
     * ID 토큰의 audience(클라이언트 ID)를 확인하여 클라이언트 타입을 자동 감지합니다.
     * 
     * @param payload Google ID 토큰 페이로드
     * @return 감지된 클라이언트 타입 (IOS, ANDROID, WEB), 감지 실패 시 null
     */
    public ClientType detectClientType(GoogleIdToken.Payload payload) {
        if (payload == null) {
            return null;
        }

        // getAudience()는 Object를 반환할 수 있으므로 String으로 변환
        Object audienceObj = payload.getAudience();
        if (audienceObj == null) {
            return null;
        }

        String audience = audienceObj.toString();

        // 클라이언트 ID와 비교하여 타입 감지
        if (iosClientId != null && !iosClientId.trim().isEmpty() && audience.equals(iosClientId)) {
            return ClientType.IOS;
        }
        if (androidClientId != null && !androidClientId.trim().isEmpty() && audience.equals(androidClientId)) {
            return ClientType.ANDROID;
        }
        if (webClientId != null && !webClientId.trim().isEmpty() && audience.equals(webClientId)) {
            return ClientType.WEB;
        }

        return null;
    }
}
