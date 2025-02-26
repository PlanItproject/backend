package com.trip.planit.User.controller;

import com.trip.planit.User.config.exception.InternalServerErrorException;
import com.trip.planit.User.dto.RegistrationResponse;
import com.trip.planit.User.dto.UserProfileResponse;
import com.trip.planit.User.dto.UserRequest;
import com.trip.planit.User.dto.UserResponse;
import com.trip.planit.User.security.JwtUtil;
import com.trip.planit.User.service.EmailService;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 API - 1단계
    @PostMapping("/register")
    @Operation(summary = "회원가입 1단계", description = "회원가입 정보를 입력하고 이메일 인증 요청")
    public RegistrationResponse registerUser(@RequestParam(required = false) String email,
                                               @RequestParam(required = false) String password,
                                               @RequestParam Boolean isGoogleLogin) {
       // 일반 -> platform = APP 자동 설정
        Platform platform = Platform.APP;

        if (Boolean.TRUE.equals(isGoogleLogin)) {
            platform = Platform.GOOGLE;

            // OAuth2 인증이 완료된 상태에서 SecurityContext에서 이메일 정보를 가져옴
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!(auth.getPrincipal() instanceof OAuth2User oauth2User)) {
                throw new BadRequestException("OAuth2 authentication information could not be found.");
            }

            email = (String) oauth2User.getAttributes().get("email");

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
                throw new BadRequestException("Email is required.");
            }

            if (password == null || password.isBlank()) {
                throw new BadRequestException("Password is required.");
            }

            userService.saveTemporaryUser(email, password, platform);
            emailService.sendVerificationCode(email);

            return RegistrationResponse.builder()
                    .googleLogin(false)
                    .build();

        }
    }

    // 회원가입 API - 2단계
    @PostMapping("/email/verify")
    @Operation(summary = "회원가입 2단계 - 이메일 인증", description = "인증 코드 검증 및 회원가입 승인")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam int verificationCode) {
        try {
            if (emailService.verifyEmailCode(email, verificationCode)) {
                return ResponseEntity.ok("Email verification completed. Please enter additional information (nickname, MBTI, gender).");
            } else {
                throw new BadRequestException("Invalid verification code.");
            }
        } catch (Exception e) {
            // 예외 발생 시 임시 사용자 삭제
            userService.deleteTemporaryUsers();
            throw new InternalServerErrorException("An unexpected error occurred while verifying the email.");
        }
    }

    // 이메일 인증코드 재전송
    @PostMapping("/email/resend")
    @Operation(summary = "이메일 인증 코드 재전송", description = "이미 등록한 이메일로 인증 코드를 재전송합니다.")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {

        // 임시 사용자 존재 여부 확인
        if (!emailService.existsTemporaryUserByEmail(email)) {
            throw new BadRequestException("No temporary user found for this email, or user is already registered.");
        }

        // 이메일 인증 코드 재전송
        emailService.sendVerificationCode(email);

        return ResponseEntity.ok("Verification code has been resent.");
    }

    // 회원가입 API - 3단계 (닉네임, MBTI, 성별 입력 + 프로필 사진 업로드 후 최종 회원가입 완료)
    @PostMapping("/register/final")
    @Operation(summary = "회원가입 3단계 - 추가 정보 입력", description = "닉네임, MBTI, 성별, 프로필 사진 입력 후 최종 회원가입 완료")
    public RegistrationResponse completeRegistration(
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestParam MBTI mbti,
            @RequestParam Gender gender,
            @RequestParam(required = false) MultipartFile profile,
            HttpServletRequest request) {

        // 구글 로그인 사용자의 경우, JWT 토큰이 Authorization 헤더에 포함되어 있음
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // jwtUtil.extractEmail() 메서드가 토큰에서 이메일을 추출한다고 가정
            String tokenEmail = jwtUtil.extractemail(token);

            // 만약 파라미터로 email이 넘어오지 않았다면 토큰에서 추출한 이메일을 사용
            if (email == null || email.isBlank()) {
                email = tokenEmail;
            }
        }

        // 이메일이 여전히 null 또는 비어있으면 예외 발생 (일반 회원가입의 경우)
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email information is required to complete the final registration.");
        }

        // 프로필 이미지가 첨부된 경우 파일 업로드 처리 후 URL을 받아옴
        String profileImageUrl = null;
        if (profile != null && !profile.isEmpty()) {
            profileImageUrl = userService.uploadProfileImage(profile);
        }

        // 최종 회원가입 처리 (S3 업로드된 URL을 전달)
        userService.completeFinalRegistration(email, nickname, mbti, gender, profileImageUrl);

        String finalToken = jwtUtil.generateToken(email);

        return RegistrationResponse.builder()
                .token(finalToken)
                .build();
    }


    // 일반 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "일반 로그인")
    public UserResponse login(@RequestBody UserRequest request) {
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
            return UserResponse.builder()
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
            return userService.getUserByEmail(email).getUser_id(); // 이메일로 User 조회 후 user_id 반환
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
