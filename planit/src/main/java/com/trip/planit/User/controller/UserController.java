package com.trip.planit.User.controller;

import com.trip.planit.User.dto.UserResponse;
import com.trip.planit.User.service.UserService;
import com.trip.planit.User.entity.*;
import com.trip.planit.User.config.exception.BadRequestException;
import com.trip.planit.User.config.exception.InternalServerErrorException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Controller(유저 API)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 API
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "회원가입")
    public ResponseEntity<UserResponse> registerUser(
            @RequestParam String loginId,
            @RequestParam String password,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam MBTI mbti,
            @RequestParam(required = false) Platform platform, // 플랫폼은 선택 사항
            @RequestParam Gender gender,
            @RequestParam Language language
    ) {
        try {
            UserResponse userResponse = userService.register(loginId, password, nickname, email, mbti, platform, gender, language);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException("회원가입 처리 중 서버 오류가 발생했습니다.");
        }
    }
}
