package com.trip.planit.User.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterGoogleRequest {
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    private String googleIdToken;
}

