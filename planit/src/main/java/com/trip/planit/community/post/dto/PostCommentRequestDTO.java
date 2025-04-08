package com.trip.planit.community.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCommentRequestDTO {

    private String content;

    public PostCommentRequestDTO(String content) {
        this.content = content;
    }
}