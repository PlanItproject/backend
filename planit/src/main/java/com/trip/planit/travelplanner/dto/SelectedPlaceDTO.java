package com.trip.planit.travelplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedPlaceDTO {
    private Long id;
    private String placeName;
    private String placeId;
    private String address;
    private Double latitude; // 위도
    private Double longitude; // 경도
}