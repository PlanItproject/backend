package com.trip.planit.MyTrip.repository;

import com.trip.planit.MyTrip.entity.HotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<HotelEntity,Long> {
}
