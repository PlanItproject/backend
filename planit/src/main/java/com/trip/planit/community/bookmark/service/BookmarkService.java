package com.trip.planit.community.bookmark.service;

import com.trip.planit.community.bookmark.entity.SavedPost;
import com.trip.planit.community.bookmark.repository.SavedPostRepository;
import com.trip.planit.community.mate.entity.MatePost;
import com.trip.planit.community.mate.repository.MateRepository;
import com.trip.planit.community.post.entity.PostCategory;
import com.trip.planit.community.post.repository.PostRepository;
import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
    private final MateRepository matePostRepository;
    private final UserRepository userRepository;

    // 1. 저장된 북마크 리스트 가져오기
    public List<SavedPost> getSavedPosts(String userId) {
        Long parsedUserId;
        try {
            parsedUserId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 유저 ID입니다.");
        }

        User user = userRepository.findByUserId(parsedUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return savedPostRepository.findAllByUser(user);
    }

//    // 2. 북마크 추가 (Post 혹은 MatePost 저장)
//    public String addBookmark(String userId, Long postId, boolean isMatePost) {
//        Long parsedUserId;
//        try {
//            parsedUserId = Long.valueOf(userId);
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("유효하지 않은 유저 ID입니다.");
//        }
//
//        User user = userRepository.findByUserId(parsedUserId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        if (isMatePost) {
//            MatePost matePost = matePostRepository.findById(postId)
//                    .orElseThrow(() -> new IllegalArgumentException("메이트 글을 찾을 수 없습니다."));
//            SavedPost savedPost = new SavedPost();
//            savedPost.setUser(user);
//            savedPost.setMatePost(matePost);
//            savedPost.setMatePost(true);
//            savedPostRepository.save(savedPost);
//        } else {
//            PostCategory post = postRepository.findById(postId)
//                    .orElseThrow(() -> new IllegalArgumentException("포스트를 찾을 수 없습니다."));
//            SavedPost savedPost = new SavedPost();
//            savedPost.setUser(user);
//            savedPost.setPost(post);
//            savedPost.setMatePost(false);
//            savedPostRepository.save(savedPost);
//        }
//        return "북마크가 추가되었습니다.";
//    }

    // 3. 북마크 삭제
    public String removeBookmark(Long savedPostId) {
        savedPostRepository.deleteById(savedPostId);
        return "북마크가 삭제되었습니다.";
    }
}