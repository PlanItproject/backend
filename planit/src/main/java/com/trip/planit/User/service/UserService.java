package com.trip.planit.User.service;

import com.trip.planit.User.dto.UserResponse;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResponse register(String loginId, String password, String nickname, String email, MBTI mbti, Platform platform, Gender gender, Language language) {
        // 기본값 설정
        if (platform == null) {
            platform = Platform.APP;
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(password);

        // User 엔티티 생성
        User user = User.builder()
                .loginId(loginId)
                .password(encryptedPassword)
                .nickname(nickname)
                .email(email)
                .mbti(mbti)
                .gender(gender)
                .platform(platform)
                .language(language)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        // 저장 후 DTO 반환
        return toUserResponse(userRepository.save(user));
    }

    // User -> UserResponse 변환
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mbti(user.getMbti())
                .platform(user.getPlatform())
                .gender(user.getGender())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
