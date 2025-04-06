package com.trip.planit.community.post.controller;

import com.trip.planit.community.post.dto.PostDto;
import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.service.PostService;
import com.trip.planit.common.aws.service.AwsS3Service;
import com.trip.planit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AwsS3Service awsS3Service;
    private final PostRepository postRepository;

    // 모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // 게시글 생성 (다중 이미지 업로드 추가)
    @PostMapping
    public ResponseEntity<String> createPost(@ModelAttribute PostDto postDto) {
        // 다중 이미지 업로드 처리
        List<String> imageUrls = null;
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            imageUrls = postDto.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty()) // 유효한 파일만 처리
                    .map(awsS3Service::uploadImageWithUrl) // S3에 업로드 후 URL 반환
                    .collect(Collectors.toList());
        }

        // Post 엔터티 생성 및 저장
        Post post = postDto.toEntity(imageUrls); // 다중 이미지 URL 전달
        postRepository.save(post);

        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostDto postDto) {
        Post updatedPost = postService.updatePost(id, postDto);
        return ResponseEntity.ok(PostDto.fromEntity(updatedPost)); // 수정된 fromEntity 사용
    }

    // 미리보기 (작성 및 수정시 동일 처리)
    @PostMapping("/preview")
    public ResponseEntity<PostDto> previewPost(@RequestBody PostDto postDto) {
        // 요청 데이터를 기반으로 미리보기용 PostDto 만들어 반환
        return ResponseEntity.ok(postDto); // 수정 내용 그대로 반환
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}