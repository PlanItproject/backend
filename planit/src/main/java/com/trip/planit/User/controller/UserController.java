package com.trip.planit.User.controller;

import com.trip.planit.User.config.exception.InternalServerErrorException;
import com.trip.planit.User.dto.*;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.security.JwtService;
import com.trip.planit.User.security.JwtUtil;
import com.trip.planit.User.service.EmailService;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.language.bm.Lang;
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
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          PasswordEncoder passwordEncoder, TemporaryUserRepository temporaryUserRepository
            , JwtService jwtService, UserRepository userRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.temporaryUserRepository = temporaryUserRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    private static final int MAX_ATTEMPTS = 5;

    @PostMapping("/language")
    @Operation(summary = "언어 설정", description = "사용자가 선택한 언어를 쿠키에 저장")
    public ResponseEntity<String> setLanguage(@RequestParam("language") String language, HttpServletResponse response) {

        // 쿠키 생성: 이름은 "language", 값은 사용자가 선택한 언어 코드
        Cookie languageCookie = new Cookie("language", language.toUpperCase());
        languageCookie.setHttpOnly(true);
        languageCookie.setPath("/");
        languageCookie.setMaxAge(7 * 24 * 60 * 60); // 예: 7일간 유지

        response.addCookie(languageCookie);

        return ResponseEntity.ok("Language cookie set to " + language.toUpperCase());
    }

    // 회원가입 API - 1단계
    @PostMapping("/register")
    @Operation(summary = "회원가입 1단계", description = "회원가입 정보를 입력")
    public RegistrationResponse registerUser(@RequestBody RegisterRequest request,
                                             @CookieValue(value = "language", required = false) Language language, HttpServletResponse response) {

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
            userService.saveTemporaryUser(email, null, platform, language);

            return RegistrationResponse.builder()
                    .googleLogin(true)
                    .language(language)
                    .build();
        } else {
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Email could not be found.");
            }

            String password = request.getPassword();

            if (password == null || password.isBlank()) {
                throw new BadRequestException("Password could not be found.");
            }

            userService.saveTemporaryUser(email, password, platform, language);

            return RegistrationResponse.builder()
                    .googleLogin(false)
                    .language(language)
                    .build();
        }
    }

    @PostMapping("/email/send")
    @Operation(summary = "회원가입 - 2단계 이메일 전송", description = "이메일 전송하기")
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        if (!temporaryUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Email does not exist.");
        }
        emailService.sendRegistrationVerificationEmail(email);
        return ResponseEntity.ok("Send Email");
    }

    // 회원가입 API - 3단계
    @PostMapping("/register/email/verify")
    @Operation(summary = "회원가입 3단계 - 이메일 인증", description = "인증 코드 검증 및 회원가입 승인")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam int verificationCode) {

        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Temporary user not found."));

        // 재시도 횟수가 5번 이상인 경우 예외 발생
        if (tempUser.getFailedAttempts() >= MAX_ATTEMPTS) { // 분리된 메서드를 호출
            emailService.checkFailedAttempts(tempUser.getFailedAttempts());
        }

        // 인증 코드 검증
        if (!emailService.verifyEmailCode(email, verificationCode)) {
            tempUser.setFailedAttempts(tempUser.getFailedAttempts() + 1);
            temporaryUserRepository.save(tempUser);
            throw new BadRequestException("Invalid verification code. Please click the resend button to try again.");
        }

        // 인증 성공: 실패 횟수 초기화 및 createdAt 업데이트
        tempUser.setFailedAttempts(0);
        tempUser.setCreatedAt(LocalDateTime.now());
        temporaryUserRepository.save(tempUser);
        return ResponseEntity.ok("Email verification completed. Please enter additional information (nickname, MBTI, gender).");
    }


    // 회원가입 중 - 이메일 인증코드 재전송
    @PostMapping("/register/email/resend")
    @Operation(summary = "이메일 인증 코드 재전송", description = "이미 등록한 이메일로 인증 코드를 재전송합니다.")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {

        // 임시 사용자 존재 여부 확인
        if (!emailService.existsTemporaryUserByEmail(email)) {
            throw new BadRequestException("No temporary user found for email: " + email + ", or user is already registered.");
        }

        try {
            emailService.sendRegistrationVerificationEmail(email);
            return ResponseEntity.ok("Verification code has been resent.");
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to resend verification code. Please try again later.");
        }
    }

    // 회원가입 4단계
    @PostMapping(value = "/register/final", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입 4단계 - 추가 정보 입력", description = "닉네임, MBTI, 성별, 프로필 사진 입력 후 최종 회원가입 완료")
    public RegistrationResponse completeRegistration(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "data") RegisterFinalRequest request,
            @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "profile") MultipartFile profile,
            HttpServletResponse response) {

        String profileImageUrl = Optional.ofNullable(profile)
                .filter(p -> !p.isEmpty())
                .map(userService::uploadProfileImage)
                .orElse(null);

        userService.completeFinalRegistration(request.getEmail(), request.getNickname(), request.getMbti(), request.getGender(), profileImageUrl);
        jwtService.addAccessTokenCookie(response, request.getEmail());
        return RegistrationResponse.builder()
                .build();
    }

    // 일반 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "일반 로그인")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(request.getEmail());

            // 비밀번호 확인
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // JWT 토큰을 쿠키에 저장 (HttpServletResponse를 통해 Set-Cookie 헤더 설정)
                jwtService.addAccessTokenCookie(response, user.getEmail());
                // 토큰은 쿠키에 저장되므로, 응답 본문에는 사용자 정보만 담아 반환
                return userService.loginResponse(user);
            } else {
                throw new IllegalArgumentException("Invalid email or password.");
            }
        } catch (Exception e) {
            // 로그인 실패 시, 필요한 경우 에러 메시지나 기본값을 담아 반환
            return LoginResponse.builder()
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

    // 언어 변경
    @PutMapping("language/change")
    @Operation(summary = "언어 변경", description = "언어 변경")
    public ResponseEntity<String> updateLanguage(@RequestParam Language language, HttpServletResponse response) {

        Cookie languageCookie = new Cookie("language", language.name());
        languageCookie.setPath("/");
        languageCookie.setHttpOnly(true);
        languageCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(languageCookie);

        Long userId = getAuthenticatedUserId();
        userService.updateUserLanguage(userId, language);

        return ResponseEntity.ok("Language updated successfully");
    }

    // 비밀번호 찾기 - 이메일 전송
    @PostMapping("/find/email/send")
    @Operation(summary = "비밀번호 찾기 - 이메일 인증 코드 전송", description = "user에서 비밀번호 변경")
    public ResponseEntity<String> resendUserVerificationEmail(@RequestParam String email) {

        if (!userService.existsByEmail(email)) {
            throw new BadRequestException("No user found for email: " + email + ", or user is not registered.");
        }

        try {
            emailService.sendPasswordResetVerificationEmail(email);
            return ResponseEntity.ok("Verification code has been resent.");
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to resend verification code. Please try again later.");
        }
    }

    // 비밀번호 찾기 - 이메일 인증
    @PostMapping("/find/email/verify")
    @Operation(summary = "마이페이지 - 비밀번호 찾기 이메일 인증", description = "비밀번호 변경을 위한 이메일 인증")
    public ResponseEntity<String> verifyUserEmail(@RequestParam String email, @RequestParam int verificationCode) {

        // 사용자(User) 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found for email: " + email));

        // 재시도 횟수 체크
        emailService.checkFailedAttempts(user.getFailedAttempts());

        // 인증 코드 검증 (비밀번호 찾기용)
        if (!emailService.verifyPasswordResetEmailCode(email, verificationCode)) {
            // 인증 실패 시 실패 횟수 증가 후 저장
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            userRepository.save(user);
            throw new BadRequestException("Invalid verification code. Please click the resend button to try again.");
        }

        // 인증 성공: 실패 횟수 초기화, 이메일 인증 여부 및 인증 시간 업데이트
        user.setFailedAttempts(0);
        userRepository.save(user);

        return ResponseEntity.ok("Email verification completed. You may now reset your password.");
    }

    // 비밀번호 찾기 - 비밀번호 재설정 (인증 완료 후)
    @PostMapping("/find/password/reset")
    @Operation(summary = "비밀번호 찾기 - 비밀번호 재설정", description = "이메일 인증 완료 후 새로운 비밀번호로 변경합니다.")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        // 사용자(User) 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found for email: " + email));

        // 새로운 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }


    // 프로필 사진 수정
    @PutMapping(value = "/profile/change", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 사진 수정", description = "기존 프로필 사진을 새로운 이미지로 교체")
    public ResponseEntity<String> updateProfileImage(
            @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("newProfileImage") MultipartFile newProfileImage
    ) {
        Long userId = getAuthenticatedUserId();

        String existingProfileUrl = userService.getProfileImageUrl(userId);

        // 3) 기존 이미지가 있다면 S3에서 삭제
        if (existingProfileUrl != null && !existingProfileUrl.isEmpty()) {
            userService.deleteFile(existingProfileUrl);
        }

        // 4) 새로운 프로필 이미지를 S3에 업로드
        String newProfileUrl = userService.uploadProfileImage(newProfileImage);

        // 5) 사용자 프로필 업데이트
        userService.updateUserProfileImage(userId, newProfileUrl);

        return ResponseEntity.ok("Profile image updated successfully.");
    }

    // 프로필 삭제
    @DeleteMapping("/profile/delete")
    @Operation(summary = "프로필 사진 삭제", description = "현재 프로필 사진을 삭제")
    public ResponseEntity<String> deleteProfileImage() {
        Long userId = getAuthenticatedUserId();

        // 현재 저장된 프로필 이미지 URL 가져오기
        String existingProfileUrl = userService.getProfileImageUrl(userId);

        if (existingProfileUrl == null || existingProfileUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("No profile image to delete.");
        }

        // S3에서 기존 프로필 이미지 삭제
        userService.deleteFile(existingProfileUrl);

        // 사용자 프로필 정보 업데이트 (프로필 이미지를 NULL로 설정)
        userService.updateUserProfileImage(userId, null);

        return ResponseEntity.ok("Profile image deleted successfully.");
    }


    // 프로필 조회 API
    @GetMapping("/profile/read")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 반환")
    public String getProfile() {
        Long userId = getAuthenticatedUserId();

        return userService.getProfileImageUrl(userId);
    }

    // 회원탈퇴
    @DeleteMapping("/user/delete")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제 - 비활성화")
    public ResponseEntity<String> deleteUser(@RequestBody DeleteReqeust deleteReqeust, HttpServletResponse response) {
        try {
            Long userId = getAuthenticatedUserId(); // 현재 로그인한 사용자 ID 가져오기
            userService.deactivate(userId, deleteReqeust);
            jwtService.clearAccessTokenCookie(response);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("An error occurred while deleting the user.");
        }
    }
}
