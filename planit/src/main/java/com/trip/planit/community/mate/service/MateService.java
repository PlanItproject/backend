package com.trip.planit.community.mate.service;

import com.trip.planit.community.mate.dto.MatePostRequestDTO;
import com.trip.planit.community.mate.dto.MatePostResponseDTO;
import com.trip.planit.community.mate.entity.MatePost;
import com.trip.planit.community.mate.repository.MateRepository;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateService {

    private final MateRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    public MatePostResponseDTO createPost(String userId, MatePostRequestDTO dto) {
        User author = findUserById(userId);

        MatePost post = MatePost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .purpose(dto.getPurpose())
                .locationName(dto.getLocationName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .author(author)
                .build();

        MatePost savedPost = postRepository.save(post);

        return toResponseDTO(savedPost);
    }

    // 특정 게시글 조회
    public MatePostResponseDTO getPostById(Long id) {
        MatePost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다."));
        return toResponseDTO(post);
    }

    // 전체 게시글 조회
    public List<MatePostResponseDTO> getAllPosts() {
        List<MatePost> posts = postRepository.findAll();
        return posts.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 게시글 수정
    public MatePostResponseDTO updatePost(String userId, Long postId, MatePostRequestDTO dto) {
        User author = findUserById(userId);

        MatePost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 수정 권한 확인 (작성자만 수정 가능)
        if (!post.getAuthor().equals(author)) {
            throw new IllegalStateException("현재 사용자는 해당 게시글을 수정할 권한이 없습니다.");
        }

        // 게시글 내용 수정
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setPurpose(dto.getPurpose());
        post.setLocationName(dto.getLocationName());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setStartDate(dto.getStartDate());
        post.setEndDate(dto.getEndDate());

        MatePost updatedPost = postRepository.save(post);

        return toResponseDTO(updatedPost);
    }

    //게시글 삭제
    public void deletePost(String userId, Long postId) {
        User author = findUserById(userId);

        MatePost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 삭제 권한 확인 (작성자만 삭제 가능)
        if (!post.getAuthor().equals(author)) {
            throw new IllegalStateException("현재 사용자는 해당 게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    //사용자 조회
    private User findUserById(String userId) {
        Long parsedUserId;

        // userId를 Long으로 변환
        try {
            parsedUserId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 유저 ID입니다.");
        }

        return userRepository.findByUserId(parsedUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    // 게시글 Entity -> ResponseDTO 변환
     
    private MatePostResponseDTO toResponseDTO(MatePost post) {
        return MatePostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .purpose(post.getPurpose().name())
                .locationName(post.getLocationName())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .authorUserId(post.getAuthor().getUserId())
                .build();
    }
}