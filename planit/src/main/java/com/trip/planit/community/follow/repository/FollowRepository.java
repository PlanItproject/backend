package com.trip.planit.community.follow.repository;

import com.trip.planit.community.follow.entity.Follow;
import com.trip.planit.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 특정 유저가 팔로우 하고 있는 사용자들
    @Query("SELECT f FROM Follow f WHERE f.follower = :follower")
    List<Follow> findFollowingsByFollower(@Param("follower") User follower);

    // 특정 유저를 팔로우 하고 있는 사용자들
    @Query("SELECT f FROM Follow f WHERE f.following = :following")
    List<Follow> findFollowersByFollowing(@Param("following") User following);

    // 팔로우 관계 여부 확인
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 특정 팔로우 관계 조회
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
}