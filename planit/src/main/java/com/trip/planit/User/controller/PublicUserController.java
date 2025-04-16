package com.trip.planit.User.controller;

import com.trip.planit.User.apiPayload.code.status.ErrorStatus;
import com.trip.planit.User.apiPayload.exception.ApiResponse;
import com.trip.planit.User.apiPayload.exception.GeneralException;
import com.trip.planit.User.config.exception.InternalServerErrorException;
import com.trip.planit.User.dto.*;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.security.JwtService;
import com.trip.planit.User.service.EmailService;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.trip.planit.User.entity.Role.ROLE_USER;

@Tag(name = "Public User")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/public/users")
public class PublicUserController {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public PublicUserController(UserService userService, EmailService emailService,
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
    public ResponseEntity<ApiResponse<String>> setLanguage(@RequestParam("language") String language, HttpServletResponse response) {

        Cookie languageCookie = new Cookie("language", language.toUpperCase());
        languageCookie.setHttpOnly(true);
        languageCookie.setPath("/");
        languageCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유지

        response.addCookie(languageCookie);

        return ResponseEntity.ok(ApiResponse.onSuccess("Language cookie set to " + language.toUpperCase()));
    }

    // 일반 회원가입 - 1단계
    @PostMapping("/register/app")
    @Operation(summary = "일반 회원가입 - 1단계", description = "이메일, 비밀번호, 이름, 휴대폰번호로 임시 회원 저장")
    public RegistrationResponse registerAppUser(@RequestBody RegisterAppRequest request,
                                                @CookieValue(value = "language", required = false) Language language) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        if (temporaryUserRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(ErrorStatus.EMAIL_PENDING_VERIFICATION);
        }

        userService.saveTemporaryUser(request.getEmail(), request.getPassword(), Platform.APP, language);

        return RegistrationResponse.builder()
                .googleLogin(false)
                .language(language)
                .build();
    }

    // 구글 회원가입 - 1단계
    @PostMapping("/register/google")
    @Operation(summary = "구글 회원가입 - 1단계", description = "OAuth 인증 완료된 이메일로 임시 회원 저장")
    public RegistrationResponse registerGoogleUser(@RequestBody RegisterGoogleRequest request,
                                                   @CookieValue(value = "language", required = false) Language language) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_CUSTOM);
        }

        String email = (String) oauth2User.getAttributes().get("email");

        if (email == null || email.isBlank()) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_CUSTOM);
        }

        userService.saveTemporaryUser(email, null, Platform.GOOGLE, language);

        return RegistrationResponse.builder()
                .googleLogin(true)
                .language(language)
                .build();
    }

    // 회원가입 API - 2단계
    @PostMapping("/email/send")
    @Operation(summary = "회원가입 - 2단계 이메일 전송", description = "이메일 전송하기")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestParam String email) {
        if (!temporaryUserRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_CUSTOM);
        }
        emailService.sendRegistrationVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.onSuccess("Send Email"));
    }

    // 회원가입 API - 3단계
    @PostMapping("/register/email/verify")
    @Operation(summary = "회원가입 3단계 - 이메일 인증", description = "인증 코드 검증 및 회원가입 승인")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String email, @RequestParam int verificationCode) {

        TemporaryUser tempUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Temporary user not found."));

        // 재시도 횟수가 5번 이상인 경우 예외 발생
        if (tempUser.getFailedAttempts() >= MAX_ATTEMPTS) {
            emailService.checkFailedAttempts(tempUser.getFailedAttempts());
        }

        // 인증 코드 검증
        if (!emailService.verifyEmailCode(email, verificationCode)) {
            tempUser.setFailedAttempts(tempUser.getFailedAttempts() + 1);
            temporaryUserRepository.save(tempUser);
            throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
        }

        // 인증 성공: 실패 횟수 초기화 및 createdAt 업데이트
        tempUser.setFailedAttempts(0);
        tempUser.setCreatedAt(LocalDateTime.now());
        temporaryUserRepository.save(tempUser);
        return ResponseEntity.ok(ApiResponse.onSuccess("Email verification completed. Please enter additional information (nickname, MBTI, gender)."));
    }


    // 회원가입 중 - 이메일 인증코드 재전송
    @PostMapping("/register/email/resend")
    @Operation(summary = "이메일 인증 코드 재전송", description = "이미 등록한 이메일로 인증 코드를 재전송합니다.")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@RequestParam String email) {

        // 임시 사용자 존재 여부 확인
        if (!emailService.existsTemporaryUserByEmail(email)) {
            throw new BadRequestException("No temporary user found for email: " + email + ", or user is already registered.");
        }

        try {
            emailService.sendRegistrationVerificationEmail(email);
            return ResponseEntity.ok(ApiResponse.onSuccess("Verification code has been resent."));
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.EMAIL_SEND_FAIL);
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
        jwtService.addAccessTokenCookie(response, request.getEmail(), ROLE_USER);
        return RegistrationResponse.builder()
                .build();
    }

    // 일반 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "일반 로그인")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
            // 이메일로 사용자 조회
            User user = userService.getUserByEmailOrNull(request.getEmail());

            // 이메일이 틀린 경우
            if (user == null) {
                throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
            }

            // 비밀번호가 틀린 경우
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                jwtService.addAccessTokenCookie(response, request.getEmail(), ROLE_USER);
                return userService.loginResponse(user);
            } else {
                throw new GeneralException(ErrorStatus.INVALID_PASSWORD);
            }
    }

    // 언어 변경
    @PutMapping("language/change")
    @Operation(summary = "언어 변경", description = "언어 변경")
    public ResponseEntity<ApiResponse<String>> updateLanguage(@RequestParam Language language, HttpServletResponse response) {

        // 1. 쿠키에 저장 (항상 수행)
        Cookie languageCookie = new Cookie("language", language.name());
        languageCookie.setPath("/");
        languageCookie.setHttpOnly(true);
        languageCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(languageCookie);

        // 2. 로그인 상태라면 DB 업데이트도 수행
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Long userId = userService.getAuthenticatedUserId();
            userService.updateUserLanguage(userId, language);
        }

        return ResponseEntity.ok(ApiResponse.onSuccess("Language updated successfully"));
    }

    // 비밀번호 찾기 - 이메일 전송
    @PostMapping("/find/email/send")
    @Operation(summary = "비밀번호 찾기 - 이메일 인증 코드 전송", description = "user에서 비밀번호 변경")
    public ResponseEntity<ApiResponse<String>> resendUserVerificationEmail(@RequestParam String email) {

        if (!userService.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        try {
            emailService.sendPasswordResetVerificationEmail(email);
            return ResponseEntity.ok(ApiResponse.onSuccess("Verification code has been resent."));
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to resend verification code. Please try again later.");
        }
    }

    // 비밀번호 찾기 - 이메일 인증
    @PostMapping("/find/email/verify")
    @Operation(summary = "마이페이지 - 비밀번호 찾기 이메일 인증", description = "비밀번호 변경을 위한 이메일 인증")
    public ResponseEntity<ApiResponse<String>> verifyUserEmail(@RequestParam String email, @RequestParam int verificationCode) {

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
            throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
        }

        // 인증 성공: 실패 횟수 초기화, 이메일 인증 여부 및 인증 시간 업데이트
        user.setFailedAttempts(0);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.onSuccess("Email verification completed. You may now reset your password."));
    }

    // 비밀번호 찾기 - 비밀번호 재설정 (인증 완료 후)
    @PostMapping("/find/password/reset")
    @Operation(summary = "비밀번호 찾기 - 비밀번호 재설정", description = "이메일 인증 완료 후 새로운 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        // 사용자(User) 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found for email: " + email));

        // 새로운 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.onSuccess("Password reset successfully."));
    }
}
