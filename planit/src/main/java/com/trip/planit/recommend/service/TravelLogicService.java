package com.trip.planit.recommend.service;

import com.trip.planit.User.entity.User;
import com.trip.planit.recommend.dto.TravelSurveyRequest;
import com.trip.planit.recommend.entity.DestinationRecommendationResult;
import com.trip.planit.recommend.repository.DestinationRecommendationResultRepository;
import com.trip.planit.recommend.repository.DestinationRecommendationResultRepository.CityCountProjection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelLogicService {

    private final DestinationRecommendationResultRepository resultRepository;

    private int parseBudget(String budget) {
        return switch (budget) {
            case "~100만" -> 100;
            case "~150만" -> 150;
            case "~200만" -> 200;
            case "~250만" -> 250;
            default -> Integer.MAX_VALUE;
        };
    }

    // 유저별 이력 조회
    public List<DestinationRecommendationResult> getHistoryByUser(User user) {
        return resultRepository.findByUser(user);
    }

    // 전체 통계 조회
    public List<CityCountProjection> getRecommendationStatistics() {
        return resultRepository.getCityRecommendationStatistics();
    }

    public String recommend(TravelSurveyRequest request, User user) {
        int budgetValue = parseBudget(request.getBudget());
        String lifestyle = request.getHousingType();
        String weather = request.getSeasonType();
        String safety = request.getSafetyLevel();

        String result;

        if (budgetValue >= 100 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains("안전")) {
            result = "치앙마이";
        } else if (budgetValue >= 100 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains(
                "위험")) {
            result = "세부";
        } else if (budgetValue >= 100 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains(
                "매우")) {
            result = "조호바루";
        } else if (budgetValue >= 150 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains(
                "안전")) {
            result = "발리";
        } else if (budgetValue >= 100 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains(
                "위험")) {
            result = "쿠알라룸푸르";
        } else if (budgetValue >= 200 && lifestyle.contains("도쿄") && weather.contains("서서") && safety.contains("매우")) {
            result = "교토";
        } else if (budgetValue >= 250 && lifestyle.contains("도쿄") && weather.contains("서서") && safety.contains("위험")) {
            result = "파리";
        } else if (budgetValue >= 150 && lifestyle.contains("디자인") && weather.contains("시원") && safety.contains("안전")) {
            result = "프라하";
        } else if (budgetValue >= 200 && lifestyle.contains("도쿄") && weather.contains("서서") && safety.contains("안전")) {
            result = "베를린";
        } else if (budgetValue >= 150 && lifestyle.contains("디자인") && weather.contains("서서") && safety.contains("안전")) {
            result = "부다페스트";
        } else if (budgetValue >= 200 && lifestyle.contains("자유로운") && weather.contains("따뜻") && safety.contains(
                "매우")) {
            result = "브리즈번";
        } else if (budgetValue >= 150 && lifestyle.contains("디자인") && weather.contains("서서") && safety.contains("안전")) {
            result = "포르투";
        } else {
            result = "추천 가능한 도시가 없습니다. 조건을 다시 확인해주세요.";
        }

        resultRepository.save(DestinationRecommendationResult.builder()
                .recommendedCity(result)
                .user(user)
                .build());

        return result;
    }
}
