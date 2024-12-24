package com.trip.planit.travelplanner.service;

import com.trip.planit.travelplanner.dto.SelectedPlaceDTO;
import com.trip.planit.travelplanner.dto.TravelPlanDTO;
import com.trip.planit.travelplanner.model.SelectedPlace;
import com.trip.planit.travelplanner.model.TravelPlan;
import com.trip.planit.travelplanner.repository.SelectedPlaceRepository;
import com.trip.planit.travelplanner.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelRouteService {

    private final TravelPlanRepository travelPlanRepository;
    private final SelectedPlaceRepository selectedPlaceRepository;

    public TravelRouteService(TravelPlanRepository travelPlanRepository, SelectedPlaceRepository selectedPlaceRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.selectedPlaceRepository = selectedPlaceRepository;
    }

    @Transactional
    public TravelPlanDTO saveTravelPlan(TravelPlanDTO travelPlanDTO) {
        TravelPlan travelPlan = createTravelPlan(travelPlanDTO);
        List<SelectedPlace> selectedPlaces = createSelectedPlaces(travelPlanDTO, travelPlan);
        travelPlan.setSelectedPlaces(selectedPlaces);
        return convertToDTO(travelPlanRepository.save(travelPlan));
    }

    private TravelPlan createTravelPlan(TravelPlanDTO dto) {
        TravelPlan travelPlan = new TravelPlan();
        travelPlan.setUserId(dto.getUserId());
        travelPlan.setDepartureDate(LocalDate.parse(dto.getDepartureDate()));
        travelPlan.setReturnDate(LocalDate.parse(dto.getReturnDate()));
        travelPlan.setDestination(dto.getDestination());
        return travelPlan;
    }

    private List<SelectedPlace> createSelectedPlaces(TravelPlanDTO dto, TravelPlan travelPlan) {
        return dto.getSelectedPlaces().stream()
                .map(placeDTO -> mapToSelectedPlace(placeDTO, travelPlan))
                .collect(Collectors.toList());
    }

    private SelectedPlace mapToSelectedPlace(SelectedPlaceDTO dto, TravelPlan travelPlan) {
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new IllegalArgumentException("위도와 경도 값이 필요합니다.");
        }
        SelectedPlace place = new SelectedPlace();
        place.setPlaceName(dto.getPlaceName());
        place.setPlaceId(dto.getPlaceId());
        place.setAddress(dto.getAddress());
        place.setLatitude(dto.getLatitude());
        place.setLongitude(dto.getLongitude());
        place.setTravelPlan(travelPlan);
        return place;
    }

    public List<SelectedPlaceDTO> getAllPlaces() {
        return selectedPlaceRepository.findAll().stream()
                .map(this::convertToSelectedPlaceDTO)
                .collect(Collectors.toList());
    }

    private SelectedPlaceDTO convertToSelectedPlaceDTO(SelectedPlace place) {
        return new SelectedPlaceDTO(
                place.getId(),
                place.getPlaceName(),
                place.getPlaceId(),
                place.getAddress(),
                place.getLatitude(), // latitude 추가
                place.getLongitude() // longitude 추가
        );
    }


    @Transactional
    public boolean deleteSelectedPlace(Long id) {
        if (selectedPlaceRepository.existsById(id)) {
            selectedPlaceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteTravelPlan(Long id) {
        if (travelPlanRepository.existsById(id)) {
            travelPlanRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private TravelPlanDTO convertToDTO(TravelPlan travelPlan) {
        return new TravelPlanDTO(
                travelPlan.getId(),
                travelPlan.getUserId(),
                travelPlan.getDepartureDate().toString(),
                travelPlan.getReturnDate().toString(),
                travelPlan.getDestination(),
                travelPlan.getSelectedPlaces().stream()
                        .map(this::convertToSelectedPlaceDTO)
                        .collect(Collectors.toList())
        );
    }
}