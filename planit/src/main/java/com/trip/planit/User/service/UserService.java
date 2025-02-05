package com.trip.planit.User.service;

import com.trip.planit.User.config.exception.BadRequestException;
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
                .build();
    }

    // 회원가입 1단계 - 임시 회원으로 저장
    public void saveTemporaryUser(String email, String password, Platform platform) {
        TemporaryUser temporaryUser = TemporaryUser.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .platform(platform)
                .build();
        temporaryUserRepository.save(temporaryUser);
    }

    // 회원가입 3단계 - 닉네임, MBTI, 성별 입력 및 최종 회원가입 자동 완료
    @Transactional
    public void completeFinalRegistration(String email, String nickname, MBTI mbti, Gender gender) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // 임시 사용자 정보 업데이트
        tempUser.setNickname(nickname);
        tempUser.setMbti(mbti);
        tempUser.setGender(gender);
        temporaryUserRepository.save(tempUser);

        // 닉네임, MBTI, 성별 입력이 끝났다면 자동으로 최종 회원가입 처리
        completeRegistration(email);
    }

    // 회원가입 - 최종 회원가입 완료
    @Transactional
    public void completeRegistration(String email) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // Google 로그인 사용자라면 비밀번호를 저장하지 않음
        String password = tempUser.getPlatform() == Platform.GOOGLE ? null : tempUser.getPassword();

        User user = User.builder()
                .email(tempUser.getEmail())
                .password(password) // Google 사용자는 null 저장
                .nickname(tempUser.getNickname())
                .mbti(tempUser.getMbti())
                .gender(tempUser.getGender())
                .platform(tempUser.getPlatform())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // 일반 회원가입 사용자의 경우, 이메일 인증 정보 삭제
        if (tempUser.getPlatform() == Platform.APP) {
            emailVerificationRepository.deleteByTemporaryUserId(tempUser.getId());
        }

        // 임시 사용자 정보 삭제
        temporaryUserRepository.delete(tempUser);
    }


    // 회원가입 - 모든 임시 회원 정보 삭제
    public void deleteTemporaryUsers() {
        emailVerificationRepository.deleteAll();
        temporaryUserRepository.deleteAll();
    }

    @Transactional
    public void deleteUserAndRelatedData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));

        // 이메일 인증 기록 삭제
        emailVerificationRepository.deleteByTemporaryUser_Email(user.getEmail());

        // 임시 사용자 정보 삭제
        temporaryUserRepository.deleteByEmail(user.getEmail());

        // 유저 삭제
        userRepository.delete(user);
    }


}