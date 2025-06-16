package com.trip.planit.User.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private String message;
    private String email;
    private boolean isGoogleLogin;
}
