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
    private String imageUrl;
    private Long userId;
    private String userName;
    private String userEmail;
    private String nickname;
    private Long positionId;
    private String positionName;
    private String positionColor;
    private Boolean isAdmin;
    private LocalDateTime joinedAt;
}

















