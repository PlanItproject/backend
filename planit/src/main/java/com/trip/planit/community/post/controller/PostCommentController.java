package com.trip.planit.community.post.controller;

import com.trip.planit.community.post.dto.PostCommentRequestDTO;
import com.trip.planit.community.post.dto.PostCommentResponseDTO;
import com.trip.planit.community.post.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post/comments")
public class PostCommentController {

    private final PostCommentService commentService;

    // 댓글 생성 API

    @PostMapping("/{postId}")
    public PostCommentResponseDTO createComment(
            @AuthenticationPrincipal(expression = "name") String userId,
            @PathVariable Long postId,
            @RequestBody @Valid PostCommentRequestDTO requestDTO) {
        return commentService.createComment(userId, postId, requestDTO);
    }

    // 특정 게시글의 댓글 리스트 조회 API

    @GetMapping("/{postId}")
    public List<PostCommentResponseDTO> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    // 댓글 수정 API

    @PutMapping("/{commentId}")
    public PostCommentResponseDTO updateComment(
            @AuthenticationPrincipal(expression = "name") String userId,
            @PathVariable Long commentId,
            @RequestBody @Valid PostCommentRequestDTO requestDTO) {
        return commentService.updateComment(userId, commentId, requestDTO);
    }

    // 댓글 삭제 API

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @AuthenticationPrincipal(expression = "name") String userId,
            @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }
}