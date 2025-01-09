package com.trip.planit.travelplanner.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanDTO {
    private Long id;
    private Long userId;
    private String departureDate;
    private String returnDate;
    private String destination;
    private List<SelectedPlaceDTO> selectedPlaces;
}