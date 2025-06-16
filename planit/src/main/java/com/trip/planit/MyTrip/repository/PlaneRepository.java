package com.trip.planit.MyTrip.repository;

import com.trip.planit.MyTrip.entity.PlaneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaneRepository extends JpaRepository<PlaneEntity, Long> {
}
