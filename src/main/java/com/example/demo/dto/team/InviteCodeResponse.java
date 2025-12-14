package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "초대 코드 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCodeResponse {
    @Schema(description = "초대 코드", example = "ABC123XY")
    private String inviteCode;
}



