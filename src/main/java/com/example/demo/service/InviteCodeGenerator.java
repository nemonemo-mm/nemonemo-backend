package com.example.demo.service;

import com.example.demo.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class InviteCodeGenerator {
    
    private final TeamRepository teamRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private final Random random = new Random();
    
    /**
     * 고유한 초대 코드를 생성합니다.
     * 
     * @return 고유한 초대 코드
     */
    public String generateUniqueInviteCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            code = generateInviteCode();
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new IllegalStateException("고유한 초대 코드를 생성할 수 없습니다.");
            }
        } while (teamRepository.findByInviteCode(code).isPresent());
        
        return code;
    }
    
    /**
     * 랜덤 초대 코드를 생성합니다.
     * 
     * @return 초대 코드
     */
    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
