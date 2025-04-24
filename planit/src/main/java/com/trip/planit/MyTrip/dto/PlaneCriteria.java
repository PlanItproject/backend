package com.trip.planit.MyTrip.dto;

import lombok.Data;

@Data
public class PlaneCriteria {
    private String origin;
    private String destination;
    private String departDate;
    private String departTime;
    private String arrivalTime;
    private int passengerCount;
}
