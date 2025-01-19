package com.trip.planit.User.dto;

import com.trip.planit.User.entity.MBTI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String email;
    private String password;
}
