package com.trip.planit.community.post.service;

import com.trip.planit.Notification.service.NotificationService;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.community.post.dto.PostCommentRequestDTO;
import com.trip.planit.community.post.dto.PostCommentResponseDTO;
import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.entity.PostComment;
import com.trip.planit.community.post.repository.PostCommentRepository;
import com.trip.planit.community.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final NotificationService notificationService;
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성

    public PostCommentResponseDTO createComment(String userId, Long postId, PostCommentRequestDTO requestDTO) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        PostComment comment = PostComment.builder()
                .content(requestDTO.getContent())
                .post(post)
                .author(user)
                .build();

        PostComment savedComment = commentRepository.save(comment);

        // 자신의 게시글에 댓글을 단 경우가 아닐 때만 알림 생성
        if (!post.getAuthor().getUserId().equals(user.getUserId())) {
            String message = String.format("%s님이 회원님의 게시글에 댓글을 달았습니다.", user.getNickname());
            notificationService.sendReplyNotification(
                    message,
                    post.getAuthor().getUserId().toString()
            );
        }


        return toResponseDTO(savedComment);
    }

    // 특정 게시글의 댓글 리스트 조회

    public List<PostCommentResponseDTO> getCommentsByPostId(Long postId) {
        Post post = findPostById(postId);

        List<PostComment> comments = commentRepository.findByPost(post);

        return comments.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 댓글 수정

    public PostCommentResponseDTO updateComment(String userId, Long commentId, PostCommentRequestDTO requestDTO) {
        User user = findUserById(userId);

        PostComment comment = findCommentById(commentId);

        if (!comment.getAuthor().equals(user)) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(requestDTO.getContent());

        // 댓글 수정 시에도 알림을 보낼 수 있습니다 (선택적)
        if (!comment.getPost().getAuthor().getUserId().equals(user.getUserId())) {
            String message = String.format("%s님이 회원님의 게시글의 댓글을 수정했습니다.", user.getNickname());
            notificationService.sendReplyNotification(
                    message,
                    comment.getPost().getAuthor().getUserId().toString()
            );
        }


        return toResponseDTO(comment);
    }

    // 댓글 삭제

    public void deleteComment(String userId, Long commentId) {
        User user = findUserById(userId);

        PostComment comment = findCommentById(commentId);

        if (!comment.getAuthor().equals(user)) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    private User findUserById(String userId) {
        return userRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private PostComment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    private PostCommentResponseDTO toResponseDTO(PostComment comment) {
        return PostCommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getUserId()) // 고유 ID
                .authorName(comment.getAuthor().getNickname()) // 사용자에게 보여질 닉네임
                .build();
    }



}