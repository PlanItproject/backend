package com.trip.planit.MyTrip.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GooglePlaceResult {
    private String place_id;
    private String name;
    private String formatted_address;
    private Geometry geometry;
}
