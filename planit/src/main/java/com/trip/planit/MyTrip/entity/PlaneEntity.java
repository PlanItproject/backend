package com.trip.planit.MyTrip.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "plane")
@Data
public class PlaneEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String planeId;
    private String origin;
    private String destination;

    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;

    private int passengerCount;
}
