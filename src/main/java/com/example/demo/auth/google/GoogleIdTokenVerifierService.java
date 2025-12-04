package com.example.demo.auth.google;

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

@Component
public class GoogleIdTokenVerifierService implements InitializingBean {

    @Value("${auth.google.client-id.ios}")
    private String iosClientId;

    @Value("${auth.google.client-id.android}")
    private String androidClientId;

    private GoogleIdTokenVerifier verifier;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> clientIds = new ArrayList<>();
        clientIds.add(iosClientId);
        clientIds.add(androidClientId);

        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(clientIds)
                .build();
    }

    public GoogleIdToken.Payload verify(String idToken) throws GeneralSecurityException, IOException {
        GoogleIdToken token = verifier.verify(idToken);
        return token != null ? token.getPayload() : null;
    }
}


