package com.trip.planit.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 ID

    private String title; // 게시글 제목
    private String content; // 게시글 내용

    @Enumerated(EnumType.STRING)
    private PostCategory category; // 게시글 카테고리

    private String locationName; // 장소 이름
    private Double latitude; // 위도
    private Double longitude; // 경도

    // 기존 단일 이미지 URL 필드 삭제
    // private String imageUrl;

    // 다중 이미지를 관리하기 위해 Image 엔티티와 1:N 관계 설정
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // Post와 Image를 연동하는 메서드
    public void addImage(Image image) {
        images.add(image);
        image.setPost(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setPost(null);
    }
}