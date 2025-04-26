package com.trip.planit.User.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.trip.planit.User.config.exception.BadRequestException;
import com.trip.planit.User.config.exception.CustomS3Exception;
import com.trip.planit.User.dto.DeleteReqeust;
import com.trip.planit.User.dto.LoginResponse;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    public User getUserByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // 로그인 - response 값
    public LoginResponse loginResponse(User user) {
        return LoginResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .gender(user.getGender())
                .platform(user.getPlatform())
                .userId(user.getUserId())
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

    // S3 삭제 메서드 예시
    public void deleteFile(String fileUrl) {
        String key = extractFileName(fileUrl);  // S3 객체 key 추출
        System.out.println("Deleting S3 object with key: " + key);  // 디버그 로그

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (AmazonServiceException e) {
            throw new CustomS3Exception("AmazonServiceException: " + e.getErrorMessage(), e);
        } catch (SdkClientException e) {
            throw new CustomS3Exception("SdkClientException: " + e.getMessage(), e);
        }
    }

    private String extractFileName(String fileUrl) {
        String httpsPrefix = "https://planitbucket123.s3.amazonaws.com/";
        String s3Prefix = "s3://planitbucket123/";

        if (fileUrl.startsWith(httpsPrefix)) {
            return fileUrl.replace(httpsPrefix, "");
        } else if (fileUrl.startsWith(s3Prefix)) {
            return fileUrl.replace(s3Prefix, "");
        }

        // 접두어가 다르면 그대로 반환하거나, 추가 처리를 할 수 있음.
        return fileUrl;
    }

    public String getProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return user.getProfile();
    }

    // 업데이트
    public void updateUserProfileImage(Long userId, String newProfileUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setProfile(newProfileUrl);
        userRepository.save(user);
    }

    // 회원가입 1단계 - 임시 회원으로 저장
    public void saveTemporaryUser(String email, String password, Platform platform, Language language) {

        TemporaryUser.TemporaryUserBuilder builder = TemporaryUser.builder()
                .email(email)
                .platform(platform)
                .language(language);

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

        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));


        // email_verification 테이블에서 해당 임시 사용자와 관련된 모든 레코드를 먼저 삭제
        emailVerificationRepository.deleteByTemporaryUserId_Email(email);

        // 닉네임, MBTI, 성별 입력이 끝났다면 자동으로 최종 회원가입 처리
        completeRegistration(email, nickname, mbti, gender, profile);

        temporaryUserRepository.delete(tempUser);
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
                .language(tempUser.getLanguage())
                .role(Role.ROLE_USER)  // 기본 역할 지정
                .build();

        userRepository.save(user);

        // 일반 회원가입 사용자의 경우, 이메일 인증 정보 삭제
        if (tempUser.getPlatform() == Platform.APP) {
            emailVerificationRepository.deleteByTemporaryUserId_TemporaryUserId(tempUser.getTemporaryUserId());
        }

        // 임시 사용자 정보 삭제
        temporaryUserRepository.delete(tempUser);
    }

    @Transactional
    public void deactivate(Long userId, DeleteReqeust deleteReqeust) {
        // "기타"를 선택한 경우 상세 사유 검증
        if(deleteReqeust.getDeleteReason() == DeleteReason.OTHER &&
                (deleteReqeust.getDeleteReason_Description() == null || deleteReqeust.getDeleteReason_Description().isEmpty())) {
            throw new BadRequestException("Please provide a detailed reason when selecting 'Other' as the withdrawal reason.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setDeleteReason(deleteReqeust.getDeleteReason());
        user.setDeleteReason_Description(deleteReqeust.getDeleteReason_Description());
        user.setActive(false);
        userRepository.save(user);

//        개발 test용 10분 후로 설정.
        user.setDeletionScheduledAt(LocalDateTime.now().plusMinutes(10));

//        개발 test용 4시간 후로 설정.
//        user.setDeletionScheduledAt(LocalDateTime.now().plusHours(4));

//        예약 시각 : 현재 시간 + 3일 후
//        user.setDeletionScheduledAt(LocalDateTime.now().plusDays(3));
    }


//  @Scheduled(cron = "0 0 * * * *")
    // 개발 test용 오전 9시로 설정
    @Scheduled(cron = "0 00 9 * * *")
    @Transactional
    public void deleteUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> usersToDelete = userRepository.findByActiveFalseAndDeletionScheduledAtBefore(now);
        if (!usersToDelete.isEmpty()) {
            for (User user : usersToDelete) {
                String profileImageUrl = user.getProfile();
                if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
                    deleteFile(profileImageUrl);
                }
            }
            userRepository.deleteAll(usersToDelete);
        }
    }

    // 언어 수정
    @Transactional
    public void updateUserLanguage(Long userId, Language language) {
        // 사용자 ID로 사용자 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 엔티티의 언어 설정 변경
        user.setLanguage(language);

        // 변경 사항 저장 (Transactional 어노테이션이 있으면 save() 호출 없이도 변경사항이 반영될 수 있음)
        userRepository.save(user);
    }

    // email로 사용자 찾기
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 로그인 된 사용자 정보를 가져옴.
    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername(); // 현재 로그인한 사용자의 이메일 가져오기
            return getUserByEmail(email).getUserId(); // 이메일로 User 조회 후 user_id 반환
        }
        throw new BadRequestException("User is not authenticated.");
    }

    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
}