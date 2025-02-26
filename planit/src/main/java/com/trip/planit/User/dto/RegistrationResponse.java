package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {
    private boolean googleLogin;
    private String token;
}
