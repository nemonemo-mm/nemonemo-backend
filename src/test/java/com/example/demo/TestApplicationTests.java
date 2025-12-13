package com.example.demo;

import com.example.demo.auth.google.GoogleIdTokenVerifierService;
import com.example.demo.auth.service.SocialAuthService;
import com.example.demo.domain.repository.PositionRepository;
import com.example.demo.domain.repository.RefreshTokenRepository;
import com.example.demo.domain.repository.TeamMemberRepository;
import com.example.demo.domain.repository.TeamRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.InviteCodeGenerator;
import com.example.demo.domain.service.PositionService;
import com.example.demo.domain.service.TeamService;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "jwt.access-token-validity-ms=3600000",
    "jwt.refresh-token-validity-ms=2592000000",
    "auth.google.client-id.ios=test-ios-client-id.apps.googleusercontent.com",
    "auth.google.client-id.android=test-android-client-id.apps.googleusercontent.com",
    "auth.google.client-id.web=test-web-client-id.apps.googleusercontent.com"
})
class TestApplicationTests {

    @MockBean
    private GoogleIdTokenVerifierService googleIdTokenVerifierService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private SocialAuthService socialAuthService;
    
    // 새로 추가한 팀 관리 관련 MockBean들
    @MockBean
    private TeamRepository teamRepository;
    
    @MockBean
    private PositionRepository positionRepository;
    
    @MockBean
    private TeamMemberRepository teamMemberRepository;
    
    @MockBean
    private TeamService teamService;
    
    @MockBean
    private PositionService positionService;
    
    @MockBean
    private InviteCodeGenerator inviteCodeGenerator;
    
    @MockBean
    private JwtAuthenticationHelper jwtAuthenticationHelper;

	@Test
	void contextLoads() {
	}

}
