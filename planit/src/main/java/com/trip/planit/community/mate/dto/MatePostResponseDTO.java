package com.trip.planit.community.mate.dto;

import com.trip.planit.community.mate.entity.MatePurpose;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MatePostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String purpose;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long authorUserId;
}