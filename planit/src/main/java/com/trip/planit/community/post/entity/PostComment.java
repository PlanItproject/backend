package com.trip.planit.community.post.entity;

import com.trip.planit.User.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Builder
    public PostComment(String content, Post post, User author) {
        this.content = content;
        this.post = post;
        this.author = author;
    }

    // 댓글 내용 수정
    public void updateContent(String newContent) {
        this.content = newContent;
    }
}