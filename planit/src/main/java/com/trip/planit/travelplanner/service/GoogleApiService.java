package com.trip.planit.travelplanner.service;

import com.trip.planit.travelplanner.config.GoogleApiConfig;
import com.trip.planit.travelplanner.dto.SelectedPlaceDTO;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleApiService {

    private final RestTemplate restTemplate;
    private final GoogleApiConfig config;
    private final AtomicLong idGenerator = new AtomicLong(1); // ID 생성기

    public GoogleApiService(RestTemplate restTemplate, GoogleApiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    // @Cacheable로 캐싱 적용
    @Cacheable(value = "placesCache", key = "#destination", unless = "#result == null or #result.isEmpty()")
    public List<SelectedPlaceDTO> getPlaces(String destination) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/textsearch/json?query=tourist+attractions+in+%s&key=%s",
                destination,
                config.getGoogleApiKey()
        );

        var response = restTemplate.getForObject(url, Map.class);
        var results = (List<Map<String, Object>>) response.get("results");

        // 데이터 변환
        return results.stream()
                .map(result -> {
                    var geometry = (Map<String, Object>) result.get("geometry");
                    var location = (Map<String, Object>) geometry.get("location");
                    Double latitude = (Double) location.get("lat"); // 위도
                    Double longitude = (Double) location.get("lng"); // 경도

                    return new SelectedPlaceDTO(
                            idGenerator.getAndIncrement(), // 고유 ID 생성
                            (String) result.get("name"), // 장소 이름
                            (String) result.get("place_id"), // 장소 ID
                            (String) result.get("formatted_address"), // 주소
                            latitude, // 위도
                            longitude // 경도
                    );
                })
                .collect(Collectors.toList());
    }
}
