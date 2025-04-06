package com.trip.planit.community.post.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService { // 'class'를 'interface'로 변경
    String uploadTempImage(MultipartFile image);
}