package com.trip.planit.User.dto;

import com.trip.planit.User.entity.Gender;
import com.trip.planit.User.entity.Language;
import com.trip.planit.User.entity.MBTI;
import com.trip.planit.User.entity.Platform;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class UserResponse {
    private String loginId;
    private String nickname;
    private String email;
    private MBTI mbti;
    private Platform platform;
    private Gender gender;
    private Language language;
    private Timestamp createdAt;
}
