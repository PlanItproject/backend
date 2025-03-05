package com.trip.planit.MyTrip.controller;

import com.trip.planit.MyTrip.config.GoogleMapsClient;
import com.trip.planit.MyTrip.dto.GooglePlacesResponse;
import com.trip.planit.MyTrip.entity.HotelEntity;
import com.trip.planit.MyTrip.service.HotelSearchService;
import com.trip.planit.MyTrip.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyTrip")
@RestController
@RequestMapping("/MyTrip")
@RequiredArgsConstructor
public class HotelController {
    private final GoogleMapsClient googleMapsClient;
    private final HotelSearchService hotelSearchService;
    private final HotelService hotelService;

    @GetMapping("/hotels")
    @Operation(summary = "호텔 정보 검색")
    public GooglePlacesResponse searchHotels(@RequestParam String query) {
        return googleMapsClient.searchHotels(query);
    }

    @PostMapping("/hotels")
    @Operation(summary = "호텔 정보 저장")
    public HotelEntity saveHotel(@RequestBody HotelEntity hotel) {
        return hotelService.save(hotel);
    }
}
