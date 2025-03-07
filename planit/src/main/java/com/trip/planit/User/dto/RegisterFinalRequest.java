package com.trip.planit.User.dto;

import com.trip.planit.User.entity.Gender;
import com.trip.planit.User.entity.MBTI;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RegisterFinalRequest {
    private String email;
    private String nickname;
    private MBTI mbti;
    private Gender gender;
}
