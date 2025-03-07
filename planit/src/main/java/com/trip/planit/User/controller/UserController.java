package com.trip.planit.User.controller;

import com.trip.planit.User.config.exception.InternalServerErrorException;
import com.trip.planit.User.dto.*;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.security.JwtUtil;
import com.trip.planit.User.service.EmailService;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Tag(name = "User")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;

    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          JwtUtil jwtUtil, PasswordEncoder passwordEncoder, TemporaryUserRepository temporaryUserRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.temporaryUserRepository = temporaryUserRepository;
    }


    // 회원가입 API - 1단계
    @PostMapping("/register")
    @Operation(summary = "회원가입 1단계", description = "회원가입 정보를 입력하고 이메일 인증 요청")
    public RegistrationResponse registerUser(@RequestBody RegisterRequest request) {

        // 일반 -> platform = APP 자동 설정
        Platform platform = Platform.APP;

        String email = request.getEmail();

        if (Boolean.TRUE.equals(request.isGoogleLogin())) {
            platform = Platform.GOOGLE;

            // OAuth2 인증이 완료된 상태에서 SecurityContext에서 이메일 정보를 가져옴
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof OAuth2User oauth2User)) {
                throw new BadRequestException("Invalid OAuth2 authentication.");
            }

            request.setEmail((String) oauth2User.getAttributes().get("email"));
            email = request.getEmail();

            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google account email could not be found.");
            }

            // Google 로그인 - OAuth에서 이메일 자동 적용
            userService.saveTemporaryUser(email, null, platform);

            String token = jwtUtil.generateToken(email);

            return RegistrationResponse.builder()
                    .googleLogin(true)
                    .token(token)
                    .build();
        } else {
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Email could not be found.");
            }

            String password = request.getPassword();

            if (password == null || password.isBlank()) {
                throw new BadRequestException("Password could not be found.");
            }

            userService.saveTemporaryUser(email, password, platform);
            emailService.sendVerificationEmail(email);

            return RegistrationResponse.builder()
                    .googleLogin(false)
                    .build();
        }
    }

    // 회원가입 API - 2단계
    @PostMapping("/email/verify")
    @Operation(summary = "회원가입 2단계 - 이메일 인증", description = "인증 코드 검증 및 회원가입 승인")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam int verificationCode) {
        if (!emailService.verifyEmailCode(email, verificationCode)) {
            throw new BadRequestException("Invalid verification code.");
        }

        // 이메일 인증 완료 후 temporary_user의 created_at 필드 업데이트
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Temporary user not found."));
        tempUser.setCreatedAt(LocalDateTime.now());
        temporaryUserRepository.save(tempUser);

        try {
            return ResponseEntity.ok("Email verification completed. Please enter additional information (nickname, MBTI, gender).");
        } catch (RuntimeException e) {
            temporaryUserRepository.deleteByTemporaryUserId(tempUser.getTemporaryUserId());
            throw new InternalServerErrorException("An unexpected error occurred while verifying the email.");
        }
    }

    // 이메일 인증코드 재전송
    @PostMapping("/email/resend")
    @Operation(summary = "이메일 인증 코드 재전송", description = "이미 등록한 이메일로 인증 코드를 재전송합니다.")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {

        // 임시 사용자 존재 여부 확인
        if (!emailService.existsTemporaryUserByEmail(email)) {
            throw new BadRequestException("No temporary user found for email: " + email + ", or user is already registered.");
        }

        try {
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok("Verification code has been resent.");
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to resend verification code. Please try again later.");
        }

    }

    // 회원가입 API - 3단계 (닉네임, MBTI, 성별 입력 + 프로필 사진 업로드 후 최종 회원가입 완료)
    @PostMapping(value = "/register/final", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입 3단계 - 추가 정보 입력", description = "닉네임, MBTI, 성별, 프로필 사진 입력 후 최종 회원가입 완료")
    public RegistrationResponse completeRegistration(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value="data") RegisterFinalRequest request,
            @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "profile") MultipartFile profile) {

        String profileImageUrl = Optional.ofNullable(profile)
                .filter(p -> !p.isEmpty())
                .map(userService::uploadProfileImage)
                .orElse(null);

        userService.completeFinalRegistration(request.getEmail(), request.getNickname(), request.getMbti(), request.getGender(), profileImageUrl);
        String token = jwtUtil.generateToken(request.getEmail());
        return RegistrationResponse.builder()
                .token(token)
                .build();
    }

    // 일반 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "일반 로그인")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {

            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(request.getEmail());

            // 비밀번호 확인
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // 토큰 생성
                String token = jwtUtil.generateToken(user.getEmail());

                // UserResponse 반환
                return userService.loginResponse(user, token);
            } else {
                throw new IllegalArgumentException("Invalid email or password.");
            }
        } catch (Exception e) {
            // 실패 시 - null
            return LoginResponse.builder()
                    .token(null)
                    .nickname(null)
                    .email(null)
                    .mbti(null)
                    .platform(null)
                    .gender(null)
                    .language(null)
                    .build();
        }
    }

    // 로그인 된 사용자 정보를 가져옴.
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername(); // 현재 로그인한 사용자의 이메일 가져오기
            return userService.getUserByEmail(email).getUserId(); // 이메일로 User 조회 후 user_id 반환
        }
        throw new BadRequestException("User is not authenticated.");
    }

    // 프로필 사진 수정
    @PutMapping("/profile/change")
    @Operation(summary = "프로필 사진 수정", description = "기존 프로필 사진을 새로운 이미지로 교체")
    public ResponseEntity<String> updateProfileImage(
            @RequestParam(required = false) MultipartFile profileImage
    ) {
        // 1) 로그인한 사용자 ID 가져오기
        Long userId = getAuthenticatedUserId();

        // 2) Service 로직 호출: 프로필 사진만 변경
        userService.updateUserProfileImage(userId, profileImage);

        return ResponseEntity.ok("Profile image updated successfully.");
    }

    // 프로필 조회 API
    @GetMapping("/profile/read")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 반환")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = getAuthenticatedUserId();
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // 회원탈퇴
    @DeleteMapping("/profile/delete")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제 - 비활성화")
    public ResponseEntity<String> deleteUser() {
        try {
            Long userId = getAuthenticatedUserId(); // 현재 로그인한 사용자 ID 가져오기
            userService.deactivate(userId);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e) {
            throw new BadRequestException("An error occurred while deleting the user.");
        }
    }

}
