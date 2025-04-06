package com.trip.planit.community.post.service;

import com.trip.planit.community.post.util.ImageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

    @Override
    public String uploadTempImage(MultipartFile image) {
        String fileName = ImageUtil.generateFileName(image.getOriginalFilename());
        String tempUrl = "https://temp-storage.com/uploads/" + fileName;
        return tempUrl;
    }

}