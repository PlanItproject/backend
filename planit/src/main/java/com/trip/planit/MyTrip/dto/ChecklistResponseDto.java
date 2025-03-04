package com.trip.planit.MyTrip.dto;

import java.util.List;
import lombok.Data;

@Data
public class ChecklistResponseDto {
    private List<ChecklistItemDto> required;
    private List<ChecklistItemDto> recommend;
    private List<ChecklistItemDto> custom;
}
