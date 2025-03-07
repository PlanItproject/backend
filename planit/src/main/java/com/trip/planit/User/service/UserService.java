package com.trip.planit.User.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.trip.planit.User.config.exception.BadRequestException;
import com.trip.planit.User.dto.UserProfileResponse;
import com.trip.planit.User.dto.LoginResponse;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import jakarta.annotation.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

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
    public LoginResponse loginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .gender(user.getGender())
                .build();
    }

    // 이미지 업로드
    public String uploadProfileImage(MultipartFile profileImage) {
        String originalFilename = profileImage.getOriginalFilename();
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        String key = "profile-images/" + savedFilename; // S3 내 저장 경로

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(profileImage.getSize());
        metadata.setContentType(profileImage.getContentType());

        try {
            amazonS3.putObject(bucketName, key, profileImage.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image to S3", e);
        }

        return amazonS3.getUrl(bucketName, key).toString();
    }

    // 이미지 업데이트
    @Transactional
    public void updateUserProfileImage(Long userId, MultipartFile profileImage) {
        // 1) DB에서 기존 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // 2) 프로필 이미지가 전달된 경우에만 업데이트
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileUrl = uploadProfileImage(profileImage);
            user.setProfile(profileUrl);
        } else {
            throw new BadRequestException("Profile image file is missing.");
        }

        // 3) 변경사항 DB 반영
        userRepository.save(user);
    }

    // 이미지 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        // DB에서 사용자 조회 (없으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // UserProfileResponse DTO에 사용자 정보 매핑
        return new UserProfileResponse(
                user.getProfile() // S3에 업로드된 프로필 이미지 URL
        );
    }

    // 회원가입 1단계 - 임시 회원으로 저장
    public void saveTemporaryUser(String email, String password, Platform platform) {

        TemporaryUser.TemporaryUserBuilder builder = TemporaryUser.builder()
                .email(email)
                .platform(platform);

        // 일반 회원가입인 경우 비밀번호 암호화 후 저장
        if (password != null && !password.isBlank()) {
            builder.password(passwordEncoder.encode(password));
        }

        TemporaryUser temporaryUser = builder.build();
        temporaryUserRepository.save(temporaryUser);
    }


    // 회원가입 3단계 - 닉네임, MBTI, 성별 입력 및 최종 회원가입 자동 완료
    @Transactional
    public void completeFinalRegistration(String email, String nickname, MBTI mbti, Gender gender, String profile) {
        // 임시 사용자가 존재하는지 확인만 함 (추가 정보는 바로 최종 등록에 사용)
        temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // 닉네임, MBTI, 성별 입력이 끝났다면 자동으로 최종 회원가입 처리
        completeRegistration(email, nickname, mbti, gender, profile);
    }

    // 회원가입 - 최종 회원가입 완료
    @Transactional
    public void completeRegistration(String email, String nickname, MBTI mbti, Gender gender, String profile) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // Google 로그인 사용자라면 비밀번호를 저장하지 않음
        String password = tempUser.getPlatform() == Platform.GOOGLE ? null : tempUser.getPassword();

        User user = User.builder()
                .email(tempUser.getEmail())
                .password(password)
                .nickname(nickname)
                .mbti(mbti)
                .gender(gender)
                .platform(tempUser.getPlatform())
                .profile(profile)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // 일반 회원가입 사용자의 경우, 이메일 인증 정보 삭제
        if (tempUser.getPlatform() == Platform.APP) {
            emailVerificationRepository.deleteByTemporaryUserId_TemporaryUserId(tempUser.getTemporaryUserId());
        }

        // 임시 사용자 정보 삭제
        temporaryUserRepository.delete(tempUser);
    }

//    public void deleteTemporaryUserByEmail(Long temporaryUserId) {
//        emailVerificationRepository.deleteByTemporaryUserId(temporaryUserId);
//        temporaryUserRepository.deleteByTemporaryUserId(temporaryUserId);
//    }

    public void deleteTemporaryUserByEmail(String email) {
        temporaryUserRepository.findByEmail(email);
    }

    @Transactional
    public void deactivate(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(false);

        // 예약 시각 : 현재 시간 + 3일 후
        user.setDeletionScheduledAt(LocalDateTime.now().plusDays(3));
    }


    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> usersToDelete = userRepository.findByActiveFalseAndDeletionScheduledAtBefore(now);
        if (!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
        }
    }
}