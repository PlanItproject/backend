package com.trip.planit.community.bookmark.controller;

import com.trip.planit.community.bookmark.entity.SavedPost;
import com.trip.planit.community.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 1. 저장된 북마크 조회
    @GetMapping
    public List<SavedPost> getBookmarks(@RequestParam String username) {
        return bookmarkService.getSavedPosts(username);
    }

    // 2. 북마크 추가 (Post or MatePost)
    @PostMapping
    public String addBookmark(
            @RequestParam String username,
            @RequestParam Long postId,
            @RequestParam boolean isMatePost) {
        return bookmarkService.addBookmark(username, postId, isMatePost);
    }

    // 3. 북마크 삭제
    @DeleteMapping("/{savedPostId}")
    public String removeBookmark(@PathVariable Long savedPostId) {
        return bookmarkService.removeBookmark(savedPostId);
    }
}