package com.example.demo.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String nickname;
    private Long roleCategoryId;
    private String roleCategoryName;
    private String roleCategoryColor;
    private Boolean isAdmin;
    private LocalDateTime joinedAt;
}







