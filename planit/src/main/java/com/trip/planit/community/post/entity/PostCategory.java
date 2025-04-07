package com.trip.planit.community.post.entity;

public enum PostCategory {
    LOCAL_NEWS("동네소식"),
    TRAVEL_TIP("여행팁"),
    TRAVEL_REVIEW("여행후기"),
    FOOD_RECOMMENDATION("맛집추천"),
    CAFE_RECOMMENDATION("카페추천"),
    ACCOMMODATION_RECOMMENDATION("숙소추천"),
    SHOPPING_RECOMMENDATION("쇼핑추천");

    private final String description;

    PostCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}