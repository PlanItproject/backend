package com.trip.planit.MyTrip.service;

import com.trip.planit.MyTrip.entity.HotelEntity;
import com.trip.planit.MyTrip.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelEntity save(HotelEntity hotel) {
        return hotelRepository.save(hotel);
    }
}
