package com.trip.planit.community.follow.service;

import com.trip.planit.community.follow.entity.Follow;
import com.trip.planit.community.follow.repository.FollowRepository;
import com.trip.planit.User.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;

    @Transactional
    public void followUser(User follower, User following) {
        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("이미 해당 사용자를 팔로우하고 있습니다.");
        }

        // Follow 객체 생성
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        // 팔로우 저장
        followRepository.save(follow);
    }


    @Transactional
    public void unfollowUser(User follower, User following) {
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new IllegalStateException("팔로우 관계가 존재하지 않습니다."));
        followRepository.delete(follow);
    }

    public List<User> getFollowings(User user) {
        // 특정 유저가 팔로우하는 사람 목록 조회
        return followRepository.findFollowingsByFollower(user).stream()
                .map(Follow::getFollowing)
                .toList();
    }

    public List<User> getFollowers(User user) {
        // 특정 유저를 팔로우하는 사람 목록 조회
        return followRepository.findFollowersByFollowing(user).stream()
                .map(Follow::getFollower)
                .toList();
    }


}
