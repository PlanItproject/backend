package com.trip.planit.User.dto;

import com.trip.planit.User.entity.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {
    private boolean googleLogin;
    private Language language;
}
