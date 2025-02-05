package com.trip.planit.User.controller;

import com.trip.planit.User.dto.UserRequest;
import com.trip.planit.User.dto.UserResponse;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.security.JwtUtil;
import com.trip.planit.User.service.EmailService;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@Tag(name = "User Controller(유저 API)")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    // 회원가입 API - 1단계 (platform 자동 설정, Google 로그인 시 OAuth에서 이메일 자동 적용)
    @PostMapping("/register")
    @Operation(summary = "회원가입 1단계", description = "회원가입 정보를 입력하고 이메일 인증 요청")
    public ResponseEntity<String> registerUser(@RequestParam(required = false) String email,
                                               @RequestParam(required = false) String password,
                                               @RequestParam(required = false) Boolean isGoogleLogin) {

        Platform platform = Platform.APP; // 기본값 APP

        if (Boolean.TRUE.equals(isGoogleLogin)) {
            platform = Platform.GOOGLE;

            // OAuth2 인증이 완료된 상태에서 SecurityContext에서 이메일 정보를 가져옴
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!(auth.getPrincipal() instanceof OAuth2User oauth2User)) {
                throw new BadRequestException("OAuth2 인증 정보를 찾을 수 없습니다.");
            }

            email = (String) oauth2User.getAttributes().get("email");

            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google 계정 이메일을 찾을 수 없습니다.");
            }

            // Google 로그인인 경우 password는 필요 없음 (null로 처리)
            userService.saveTemporaryUser(email, null, platform);

            return ResponseEntity.ok("Google 로그인 감지됨. 추가 정보를 입력하여 최종 회원가입을 완료해 주세요.");
        } else {
            // 일반 회원가입의 경우 password 필수
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Email is required.");
            }
            if (password == null || password.isBlank()) {
                throw new BadRequestException("Password is required.");
            }

            userService.saveTemporaryUser(email, password, platform);
            emailService.sendVerificationCode(email);

            return ResponseEntity.ok("Your registration request has been received. Please complete email verification.");
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
            throw e; // 예외를 다시 던져서 상위에서 처리
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

    // 회원가입 API - 3단계
    @PostMapping("/register/final")
    @Operation(summary = "회원가입 3단계 - 추가 정보 입력", description = "닉네임, MBTI, 성별 입력 후 최종 회원가입 완료")
    public ResponseEntity<String> completeRegistration(
            @RequestParam(required = false) String email, // 일반 회원가입만 입력 필요
            @RequestParam String nickname,
            @RequestParam MBTI mbti,
            @RequestParam Gender gender) {

        // Google 로그인 사용자 검사 - 프론트한테 넘기기

        // 임시 사용자 정보 업데이트 및 최종 회원가입 처리
        userService.completeFinalRegistration(email, nickname, mbti, gender);
        return ResponseEntity.ok("Final registration has been completed.");
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

    // 회원탈퇴
    @DeleteMapping("/profile/delete")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제")
    public ResponseEntity<String> deleteUser() {
        try {
            Long userId = getAuthenticatedUserId(); // 현재 로그인한 사용자 ID 가져오기
            userService.deleteUserAndRelatedData(userId);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e) {
            throw new BadRequestException("An error occurred while deleting the user.");
        }
    }

}
