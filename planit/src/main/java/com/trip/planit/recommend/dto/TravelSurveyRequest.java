package com.trip.planit.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TravelSurveyRequest {
    private String budget;            // "~100만"
    private String housingType;       // "자유로운 현지 라이프"
    private String seasonType;        // "따뜻하고 더운 날씨"
    private String safetyLevel;       // "대부분 안전"
}
