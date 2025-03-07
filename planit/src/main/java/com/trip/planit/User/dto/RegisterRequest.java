package com.trip.planit.User.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    @Schema(defaultValue = "false", example = "false")
    private boolean isGoogleLogin;
}
