package com.trip.planit.User.service;

import com.trip.planit.User.dto.UserResponse;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    // 로그인 - User에서 Email 찾기
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
    }

    // 로그인 - response 값
    public UserResponse loginResponse(User user, String token) {
        return UserResponse.builder()
                .token(token)
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .gender(user.getGender())
                .language(user.getLanguage())
                .build();
    }

    // 회원가입 - 임시 회원으로 저장
    public void saveTemporaryUser(String email, String password, String nickname, MBTI mbti, Gender gender, Platform platform, Language language) {
        TemporaryUser temporaryUser = TemporaryUser.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .mbti(mbti)
                .gender(gender)
                .platform(platform)
                .language(language)
                .build();
        temporaryUserRepository.save(temporaryUser);
    }

    // 회원가입 - 최종 회원가입 완료
    @Transactional
    public void completeRegistration(String email) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        User user = User.builder()
                .email(tempUser.getEmail())
                .password(tempUser.getPassword())
                .nickname(tempUser.getNickname())
                .mbti(tempUser.getMbti())
                .gender(tempUser.getGender())
                .language(tempUser.getLanguage())
                .platform(tempUser.getPlatform())
                .createdAt(LocalDateTime.now())
                .build();


        userRepository.save(user);
        emailVerificationRepository.deleteByTemporaryUserId(tempUser.getId());
        temporaryUserRepository.delete(tempUser);
    }

    // 회원가입 - 모든 임시 회원 정보 삭제
    public void deleteTemporaryUsers() {
        emailVerificationRepository.deleteAll();
        temporaryUserRepository.deleteAll();
    }
}