package com.trip.planit.User.dto;

import com.trip.planit.User.entity.MBTI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String loginId;
    private String password;
    private String name;
    private String nickname;
    private String language;
    private MBTI mbti;
    private String profile;
}
