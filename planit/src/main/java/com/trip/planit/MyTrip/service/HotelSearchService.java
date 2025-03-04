package com.trip.planit.MyTrip.service;

import com.trip.planit.MyTrip.config.GoogleMapsClient;
import com.trip.planit.MyTrip.dto.GooglePlaceResult;
import com.trip.planit.MyTrip.dto.GooglePlacesResponse;
import com.trip.planit.MyTrip.entity.HotelEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelSearchService {
    private final GoogleMapsClient googleMapsClient;

    public HotelEntity mapFirstHotel(String query) {
        GooglePlacesResponse response = googleMapsClient.searchHotels(query);
        if(response == null || response.getResults() == null || response.getResults().isEmpty())
            throw new RuntimeException("No hotels found for query: " + query);
        GooglePlaceResult result = response.getResults().get(0);
        return mapToEntity(result);
    }

    private HotelEntity mapToEntity(GooglePlaceResult result) {
        HotelEntity hotel = new HotelEntity();
        hotel.setPlaceId(result.getPlace_id());
        hotel.setName(result.getName());
        hotel.setAddress(result.getFormatted_address());
        hotel.setLatitude(result.getGeometry().getLocation().getLat());
        hotel.setLongitude(result.getGeometry().getLocation().getLng());
        return hotel;
    }
}
