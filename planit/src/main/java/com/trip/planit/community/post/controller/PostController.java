package com.trip.planit.community.post.controller;

import com.trip.planit.User.entity.User;
import com.trip.planit.community.post.dto.PostDto;
import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.service.PostService;
import com.trip.planit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.trip.planit.community.follow.service.FollowService;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final FollowService followService;


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
  

    // 3. 팔로우한 유저의 게시글 조회
    @GetMapping("/followed")
    public ResponseEntity<List<PostDto>> getFollowedPosts(@AuthenticationPrincipal User currentUser) {
        // 1. 팔로우한 유저 가져오기
        List<User> followings = followService.getFollowings(currentUser);

        // 2. 팔로우한 유저들의 게시글 가져오기
        List<Post> followedPosts = postRepository.findByAuthorInOrderByCreatedAtDesc(followings);

        // 3. DTO 변환 및 반환
        List<PostDto> result = followedPosts.stream()
                .map(postService::toDto) // Post -> PostDto
                .toList();

        return ResponseEntity.ok(result);
    }


    // 게시글 생성 (다중 이미지 업로드 추가)
    @PostMapping
    public ResponseEntity<String> createPost(@ModelAttribute PostDto postDto) {
        // 다중 이미지 업로드 처리
        List<String> imageUrls = null;
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            imageUrls = postDto.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty()) // 유효한 파일만 처리
                    .map(file -> uploadImageToLocal(file)) // 로컬에 저장 후 URL 반환
                    .collect(Collectors.toList());
        }

        // Post 엔터티 생성 및 저장
        Post post = postDto.toEntity(imageUrls); // 다중 이미지 URL 전달
        postRepository.save(post);

        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    // 로컬에 이미지를 저장하고 URL을 반환하는 메소드
    private String uploadImageToLocal(MultipartFile file) {
        try {
            // 저장할 경로 설정 (예: "uploads/")
            String uploadDir = "uploads/";
            String filePath = uploadDir + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 로컬 경로에 파일 저장
            File destFile = new File(filePath);
            destFile.getParentFile().mkdirs(); // 디렉토리 생성
            file.transferTo(destFile);

            // 서버에서 접근 가능한 URL 반환 (설정 필요)
            return "/images/" + destFile.getName();
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 실패: " + e.getMessage());
        }
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