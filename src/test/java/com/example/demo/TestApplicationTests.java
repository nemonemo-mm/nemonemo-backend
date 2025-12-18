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
import com.example.demo.service.DeviceTokenService;
import com.example.demo.service.TodoService;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.FcmNotificationService;
import com.example.demo.service.TeamNotificationSettingService;
import com.example.demo.service.PersonalNotificationSettingService;
import com.example.demo.service.TeamPermissionService;
import com.example.demo.service.FirebaseStorageService;
import com.example.demo.repository.DeviceTokenRepository;
import com.example.demo.repository.TodoRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.TodoAttendeeRepository;
import com.example.demo.repository.TodoPositionRepository;
import com.example.demo.repository.ScheduleAttendeeRepository;
import com.example.demo.repository.SchedulePositionRepository;
import com.example.demo.repository.NotificationSettingRepository;
import com.example.demo.repository.PersonalNotificationSettingRepository;
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
    
    // 새로 추가한 서비스 및 리포지토리 MockBean들
    @MockBean
    private DeviceTokenService deviceTokenService;
    
    @MockBean
    private TodoService todoService;
    
    @MockBean
    private ScheduleService scheduleService;
    
    @MockBean
    private FcmNotificationService fcmNotificationService;
    
    @MockBean
    private TeamNotificationSettingService teamNotificationSettingService;
    
    @MockBean
    private PersonalNotificationSettingService personalNotificationSettingService;
    
    @MockBean
    private TeamPermissionService teamPermissionService;
    
    @MockBean
    private FirebaseStorageService firebaseStorageService;
    
    @MockBean
    private DeviceTokenRepository deviceTokenRepository;
    
    @MockBean
    private TodoRepository todoRepository;
    
    @MockBean
    private ScheduleRepository scheduleRepository;
    
    @MockBean
    private TodoAttendeeRepository todoAttendeeRepository;
    
    @MockBean
    private TodoPositionRepository todoPositionRepository;
    
    @MockBean
    private ScheduleAttendeeRepository scheduleAttendeeRepository;
    
    @MockBean
    private SchedulePositionRepository schedulePositionRepository;
    
    @MockBean
    private NotificationSettingRepository notificationSettingRepository;
    
    @MockBean
    private PersonalNotificationSettingRepository personalNotificationSettingRepository;

	@Test
	void contextLoads() {
	}

}
