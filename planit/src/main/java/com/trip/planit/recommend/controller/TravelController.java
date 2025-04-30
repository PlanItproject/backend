package com.trip.planit.recommend.controller;

import com.trip.planit.User.entity.User;
import com.trip.planit.recommend.dto.TravelSurveyRequest;
import com.trip.planit.recommend.entity.DestinationRecommendationResult;
import com.trip.planit.recommend.repository.DestinationRecommendationResultRepository.CityCountProjection;
import com.trip.planit.recommend.service.TravelLogicService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TravelController {

    private final TravelLogicService logicService;

    @PostMapping("/recommend")
    @Operation(summary = "추천로직", description = ""
            + "ex) {\n"
            + "  \"budget\": \"~150만원\",\n"
            + "  \"housingType\": \"자유로운 현지 라이프\",\n"
            + "  \"seasonType\": \"따뜻하고 더운 날씨\",\n"
            + "  \"safetyLevel\": \"대부분 안전\"\n"
            + "}"
            + "= 치앙마이")
    public ResponseEntity<String> recommend(@RequestBody TravelSurveyRequest request,
                                            @AuthenticationPrincipal User user) {
        String result = logicService.recommend(request, user);
        return ResponseEntity.ok(result);
    }

    // 유저 이력 조회
    @GetMapping("/recommend/history")
    @Operation(summary = "유저 지역 추천 장소 횟수", description = "유저 지역 추천 횟수")
    public ResponseEntity<List<DestinationRecommendationResult>> getHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(logicService.getHistoryByUser(user));
    }

    // 전체 통계 조회
    @GetMapping("/recommend/statistics")
    @Operation(summary = "전체 통계 조회", description = "전체 통계 조회(관리자 전용)")
    public ResponseEntity<List<CityCountProjection>> getStats() {
        return ResponseEntity.ok(logicService.getRecommendationStatistics());
    }
}
