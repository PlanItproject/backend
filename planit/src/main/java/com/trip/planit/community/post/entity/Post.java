package com.trip.planit.community.post.entity;

import com.trip.planit.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt; // 작성 시간 필드 추가

    @PrePersist // 엔티티 저장 전에 호출 - 자동으로 생성 시간 설정
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "author_id") // 외래 키 매핑: User 테이블의 user_id
    private User author; // 작성자를 User 객체로 매핑


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