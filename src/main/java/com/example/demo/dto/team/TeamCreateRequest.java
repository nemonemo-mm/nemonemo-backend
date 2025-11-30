package com.example.demo.dto.team;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamCreateRequest {
    @NotBlank
    private String name;
}

