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
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Controller(유저 API)")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 API
    @PostMapping("/register")
    @Operation(summary = "회원가입 요청", description = "회원가입 정보를 입력하고 이메일 인증 요청")
    public ResponseEntity<String> registerUser(@RequestParam String email,
                                               @RequestParam String password,
                                               @RequestParam String nickname,
                                               @RequestParam MBTI mbti,
                                               @RequestParam Gender gender,
                                               @RequestParam Platform platform,
                                               @RequestParam Language language) {
        userService.saveTemporaryUser(email, password, nickname, mbti, gender, platform, language); // 임시 회원 저장
        emailService.sendVerificationCode(email);   // 이메일 인증 보내기
        return ResponseEntity.ok("Your registration request has been received. Please complete email verification.");
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증", description = "인증 코드 검증 및 회원가입 승인")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam int verificationCode) {
        try {
            if (emailService.verifyEmailCode(email, verificationCode)) {
                userService.completeRegistration(email);
                return ResponseEntity.ok("Registration has been completed.");
            } else {
                throw new BadRequestException("Invalid verification code.");
            }
        } catch (Exception e) {
            // 예외 발생 시 임시 사용자 삭제
            userService.deleteTemporaryUsers();
            throw e; // 예외를 다시 던져서 상위에서 처리
        }
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
}
