package com.trip.planit.MyTrip.dto;

import lombok.Data;

@Data
public class ChecklistItemDto {
    private Long Id;
    private String description;
    private String type;
    private boolean isChecked;
}
