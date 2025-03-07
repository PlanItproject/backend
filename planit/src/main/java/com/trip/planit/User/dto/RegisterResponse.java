package com.trip.planit.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {
    private String email;
    private boolean isGoogleLogin;

}
