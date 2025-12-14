package com.example.demo;

import com.example.demo.service.FirebaseIdTokenVerifierService;
import com.example.demo.service.SocialAuthService;
import com.example.demo.repository.PositionRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.InviteCodeGenerator;
import com.example.demo.service.PositionService;
import com.example.demo.service.TeamService;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class TestApplicationTests {

    @MockBean
    private FirebaseIdTokenVerifierService firebaseIdTokenVerifierService;
    
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
