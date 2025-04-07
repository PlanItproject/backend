package com.trip.planit.community.bookmark.repository;

import com.trip.planit.community.bookmark.entity.SavedPost;
import com.trip.planit.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findAllByUser(User user); // 특정 사용자의 데이터 조회
}
