package com.trip.planit.community.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY) // Post와 다대일 관계 설정
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}