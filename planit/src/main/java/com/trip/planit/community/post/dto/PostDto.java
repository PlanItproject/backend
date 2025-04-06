package com.trip.planit.community.post.dto;

import com.trip.planit.community.post.entity.Image;
import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.entity.PostCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostDto {
    private Long id; // 게시글 ID
    private String title; // 제목
    private String content; // 본문
    private PostCategory category; // 카테고리
    private String locationName; // 장소 이름
    private Double latitude; // 위도
    private Double longitude; // 경도
    private List<MultipartFile> images; // 다중 이미지 데이터
    private List<String> imageUrls;


    public Post toEntity(List<String> imageUrls) {
        Post post = Post.builder()
                .title(this.title)
                .content(this.content)
                .build();

        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                Image image = Image.builder()
                        .imageUrl(imageUrl)
                        .post(post)
                        .build();
                post.addImage(image);
            }
        }

        return post;
    }


    // Entity -> DTO 변환
    public static PostDto fromEntity(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());
        dto.setLocationName(post.getLocationName());
        dto.setLatitude(post.getLatitude());
        dto.setLongitude(post.getLongitude());

        // Post 엔티티에 연결된 이미지 리스트를 DTO로 변환
        List<String> imageUrls = post.getImages().stream()
                .map(Image::getImageUrl) // Image 엔티티에서 URL만 추출
                .collect(Collectors.toList());
        dto.setImageUrls(imageUrls); // DTO에 이미지 URLs 추가

        return dto;
    }
}