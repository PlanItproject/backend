package com.trip.planit.community.post.util;

public class ImageUtil {

    public static String generateFileName(String originalName) {
        return System.currentTimeMillis() + "_" + originalName;
    }
}
