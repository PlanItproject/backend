package com.trip.planit.community.post.service;

import com.trip.planit.community.post.dto.PostDto;
import com.trip.planit.community.post.entity.Image;
import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 모든 게시글 조회
    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 게시글 조회
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));
        return PostDto.fromEntity(post);
    }

    // 게시글 작성
    public PostDto createPost(PostDto postDto) {
        List<String> imageUrls = null;

        // 다중 이미지 업로드 처리
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            imageUrls = postDto.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty()) // 유효한 파일만 처리
                    .map(this::uploadImageToLocal) // 로컬 업로드 후 URL 반환
                    .collect(Collectors.toList());
        }

        // 게시글 저장
        Post post = postRepository.save(postDto.toEntity(imageUrls));
        return PostDto.fromEntity(post);
    }

    public Post updatePost(Long id, PostDto postDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));

        // 제목 및 본문 업데이트
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        // 카테고리 업데이트
        post.setCategory(postDto.getCategory());

        // 장소 태그 업데이트
        post.setLocationName(postDto.getLocationName());
        post.setLatitude(postDto.getLatitude());
        post.setLongitude(postDto.getLongitude());

        // 이미지 수정 처리
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            // 기존 이미지 제거 (필요 시)
            post.getImages().clear();

            // 새로 업로드된 이미지 처리
            List<String> imageUrls = postDto.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(this::uploadImageToLocal) // 로컬 업로드
                    .collect(Collectors.toList());

            // 새로운 이미지 URL 리스트를 Post에 저장
            for (String imageUrl : imageUrls) {
                Image image = Image.builder()
                        .imageUrl(imageUrl)
                        .post(post)
                        .build();
                post.addImage(image);
            }
        }

        // 변경된 데이터를 저장
        return postRepository.save(post);
    }

    // 게시글 삭제
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("해당 게시글이 존재하지 않아 삭제할 수 없습니다.");
        }
        postRepository.deleteById(id);
    }

    // 로컬 디렉토리에 이미지를 저장하고 URL을 반환하는 메소드
    private String uploadImageToLocal(MultipartFile file) {
        try {
            // 저장할 디렉토리 경로 설정 (예: uploads/ 디렉토리)
            String uploadDir = "uploads/";
            String filePath = uploadDir + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 디렉토리가 없으면 생성
            File destFile = new File(filePath);
            destFile.getParentFile().mkdirs();

            // 파일 저장
            file.transferTo(destFile);

            // 저장된 파일의 URL 반환
            return "/images/" + destFile.getName();
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드에 실패하였습니다: " + e.getMessage());
        }
    }
}