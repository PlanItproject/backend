package com.trip.planit.community.mate.controller;

import com.trip.planit.community.mate.dto.MatePostRequestDTO;
import com.trip.planit.community.mate.dto.MatePostResponseDTO;
import com.trip.planit.community.mate.service.MateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
@Validated
public class MateController {

    private final MateService postService;

    // 여행 메이트 게시글 작성 API

    @PostMapping("/create")
    public MatePostResponseDTO createMatePost(
            @AuthenticationPrincipal(expression = "name") String username,
            @RequestBody @Valid MatePostRequestDTO dto) {
        return postService.createPost(username, dto);
    }

    // 특정 여행 메이트 게시글 조회 API

    @GetMapping("/{id}")
    public MatePostResponseDTO getMatePost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    // 여행 메이트 게시글 전체 조회 API

    @GetMapping("/all")
    public List<MatePostResponseDTO> getAllMatePosts() {
        return postService.getAllPosts();
    }

    // 여행 메이트 게시글 수정 API

    @PutMapping("/{id}")
    public MatePostResponseDTO updateMatePost(
            @AuthenticationPrincipal(expression = "name") String username,
            @PathVariable Long id,
            @RequestBody @Valid MatePostRequestDTO dto) {
        return postService.updatePost(username, id, dto);
    }

    // 여행 메이트 게시글 삭제 API

    @DeleteMapping("/{id}")
    public void deleteMatePost(
            @AuthenticationPrincipal(expression = "name") String username,
            @PathVariable Long id) {
        postService.deletePost(username, id);
    }
}