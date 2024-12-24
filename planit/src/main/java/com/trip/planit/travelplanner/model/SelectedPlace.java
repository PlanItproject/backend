package com.trip.planit.travelplanner.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "selected_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelectedPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private String placeId;

    @Column(nullable = false)
    private String address;

    @Column
    private Double rating;

    @Column
    private Integer userRatingsTotal;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "travel_plan_id")
    @JsonBackReference
    private TravelPlan travelPlan;
}
