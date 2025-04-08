package com.trip.planit.community.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostCommentResponseDTO {
    private Long id;
    private String content;
    private Long authorId;     // ID (내부 식별자)
    private String authorName; // UI에 표시되는 닉네임
}
