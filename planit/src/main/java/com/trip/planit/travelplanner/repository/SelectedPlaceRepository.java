package com.trip.planit.travelplanner.repository;

import com.trip.planit.travelplanner.model.SelectedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectedPlaceRepository extends JpaRepository<SelectedPlace, Long> {
}