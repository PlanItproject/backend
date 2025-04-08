package com.trip.planit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private String profileImageUrl;

    public boolean isEmpty() {
        return profileImageUrl == null || profileImageUrl.trim().isEmpty();
    }
}
