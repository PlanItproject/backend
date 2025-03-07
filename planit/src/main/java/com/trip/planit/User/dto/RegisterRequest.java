package com.trip.planit.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private boolean isGoogleLogin = false;
}
