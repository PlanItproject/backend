package com.trip.planit.community.post.controller;

import com.trip.planit.community.post.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageService imageService;

    // 생성자 주입
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/preview")
    public ResponseEntity<List<String>> previewImages(@RequestParam("images") List<MultipartFile> images) {
        List<String> previewUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            String previewUrl = imageService.uploadTempImage(image); // 이미지 처리 로직 호출
            previewUrls.add(previewUrl);
        }

        return ResponseEntity.ok(previewUrls);
    }
}