package com.trip.planit.community.follow.controller;

import com.trip.planit.User.service.UserService;
import com.trip.planit.community.follow.service.FollowService;
import com.trip.planit.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    // 팔로우 요청
    @PostMapping("/{followingId}")
    public void follow(@PathVariable Long followingId) {
        // 로그인된 사용자 ID 가져오기
        Long authenticatedUserId = userService.getAuthenticatedUserId();

        // follower와 following의 User 객체 생성
        User follower = new User();
        follower.setUserId(authenticatedUserId); // 임시 User 객체
        User following = new User();
        following.setUserId(followingId);

        // 팔로우 서비스 호출
        followService.followUser(follower, following);
    }

    // 언팔로우 요청
    @DeleteMapping("/{followingId}")
    public void unfollow(@PathVariable Long followingId) {
        // 로그인된 사용자 ID 가져오기
        Long authenticatedUserId = userService.getAuthenticatedUserId();

        // follower와 following의 User 객체 생성
        User follower = new User();
        follower.setUserId(authenticatedUserId);
        User following = new User();
        following.setUserId(followingId);

        // 언팔로우 서비스 호출
        followService.unfollowUser(follower, following);
    }

    // 팔로잉 목록 조회
    @GetMapping("/following/{userId}")
    public List<User> getFollowings(@PathVariable Long userId) {
        User user = new User();
        user.setUserId(userId); // 임시 User 객체
        return followService.getFollowings(user);
    }
}

