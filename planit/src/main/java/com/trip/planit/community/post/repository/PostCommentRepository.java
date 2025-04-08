package com.trip.planit.community.post.repository;

import com.trip.planit.community.post.entity.Post;
import com.trip.planit.community.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPost(Post post);

}