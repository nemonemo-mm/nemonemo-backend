package com.example.demo.dto.team;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCategoryCreateRequest {
    @NotBlank
    private String name;
    private String colorHex;
    private Boolean isDefault;
}






