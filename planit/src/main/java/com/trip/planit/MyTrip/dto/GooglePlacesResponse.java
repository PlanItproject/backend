package com.trip.planit.MyTrip.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GooglePlacesResponse {
    private java.util.List<GooglePlaceResult> results;
    private String status;
}

