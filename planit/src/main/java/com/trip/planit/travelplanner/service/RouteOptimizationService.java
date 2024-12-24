package com.trip.planit.travelplanner.service;

import com.trip.planit.travelplanner.config.GoogleApiConfig;
import com.trip.planit.travelplanner.model.SelectedPlace;
import com.trip.planit.travelplanner.model.TravelPlan;
import com.trip.planit.travelplanner.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteOptimizationService {

    private final TravelPlanRepository travelPlanRepository;
    private final RestTemplate restTemplate;
    private final String apiKey;

    public RouteOptimizationService(TravelPlanRepository travelPlanRepository,
                                    RestTemplate restTemplate,
                                    GoogleApiConfig config) {
        this.travelPlanRepository = travelPlanRepository;
        this.restTemplate = restTemplate;
        this.apiKey = config.getGoogleApiKey();
    }

    public List<String> calculateOptimizedRoute(Long planId, String mode) {
        TravelPlan travelPlan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        List<SelectedPlace> places = travelPlan.getSelectedPlaces();
        if (places.size() < 2) {
            throw new IllegalArgumentException("두 개 이상의 장소가 필요합니다.");
        }

        List<String> routes = new ArrayList<>();
        for (int i = 0; i < places.size() - 1; i++) {
            String origin = String.format("%f,%f", places.get(i).getLatitude(), places.get(i).getLongitude());
            String destination = String.format("%f,%f", places.get(i + 1).getLatitude(),
                    places.get(i + 1).getLongitude());
            routes.add(calculateRoute(origin, destination, mode));
        }

        return routes;
    }

    private String calculateRoute(String origin, String destination, String mode) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&mode=%s&key=%s&departure_time=%d",
                origin, destination, mode, apiKey, System.currentTimeMillis() / 1000
        );

        var response = restTemplate.getForObject(url, Map.class);
        var routes = (List<Map<String, Object>>) response.get("routes");

        if (routes == null || routes.isEmpty()) {
            if (mode.equals("transit")) {
                // 대중교통 경로가 없을 경우 운전 모드로 전환
                return calculateRoute(origin, destination, "driving");
            }
            throw new IllegalArgumentException("경로를 찾을 수 없습니다.");
        }

        var legs = (List<Map<String, Object>>) routes.get(0).get("legs");
        var duration = (Map<String, Object>) legs.get(0).get("duration");
        var distance = (Map<String, Object>) legs.get(0).get("distance");

        return String.format("소요 시간: %s, 거리: %s",
                duration.get("text"),
                distance.get("text")
        );
    }
}