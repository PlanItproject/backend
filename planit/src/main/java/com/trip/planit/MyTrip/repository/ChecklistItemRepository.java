package com.trip.planit.MyTrip.repository;

import com.trip.planit.MyTrip.dto.ChecklistItemType;
import com.trip.planit.MyTrip.entity.ChecklistEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistEntity,Long> {
    List<ChecklistEntity> findByCountry(String country);
    List<ChecklistEntity> findByCountryAndType(String country, ChecklistItemType type);
}
