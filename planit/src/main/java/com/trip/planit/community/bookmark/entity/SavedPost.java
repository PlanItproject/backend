package com.trip.planit.community.bookmark.entity;

import com.trip.planit.community.mate.entity.MatePost;
import com.trip.planit.community.post.entity.PostCategory;
import com.trip.planit.User.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SavedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) // 또는 EnumType.ORDINAL
    @Column(name = "post_id")
    private PostCategory post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_post_id")
    private MatePost matePost;

    @Column(nullable = false)
    private boolean isMatePost; // 메이트 구인글 여부
}