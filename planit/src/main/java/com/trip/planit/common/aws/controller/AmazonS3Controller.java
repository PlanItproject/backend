package com.trip.planit.common.aws.controller;

import com.trip.planit.common.aws.dto.ApiResponse;
import com.trip.planit.common.aws.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
@CrossOrigin
public class AmazonS3Controller {
    private final AwsS3Service awsS3Service;
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestPart MultipartFile multipartFile) {
        return ApiResponse.success(awsS3Service.uploadImage(multipartFile));
    }
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(@RequestParam String fileName) {
        awsS3Service.deleteImage(fileName);
        return ApiResponse.success(null);
    }
}
