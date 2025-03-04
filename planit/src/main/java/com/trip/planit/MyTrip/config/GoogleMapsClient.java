package com.trip.planit.MyTrip.config;

import com.trip.planit.MyTrip.dto.GooglePlacesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleMapsClient {
    private final String apiKey;
    private final RestTemplate restTemplate;

    public GoogleMapsClient(@Value("${google.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public GooglePlacesResponse searchHotels(String query) {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                + query + "&key=" + apiKey;
        return restTemplate.getForObject(url, GooglePlacesResponse.class);
    }
}
