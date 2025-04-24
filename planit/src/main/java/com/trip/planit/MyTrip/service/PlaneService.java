package com.trip.planit.MyTrip.service;

import com.trip.planit.MyTrip.entity.PlaneEntity;
import com.trip.planit.MyTrip.repository.PlaneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaneService {
    private final PlaneRepository planeRepository;

    public PlaneEntity save(PlaneEntity planeInfo) {
        return planeRepository.save(planeInfo);
    }
}
