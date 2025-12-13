package com.example.demo.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberListItemResponse {
    private Long id;
    private String displayName; // 닉네임 혹은 사용자 이름
    private String positionName;
    private String imageUrl;
}

