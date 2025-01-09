package com.trip.planit.travelplanner.service;

import com.trip.planit.travelplanner.dto.SelectedPlaceDTO;
import com.trip.planit.travelplanner.dto.TravelPlanDTO;
import com.trip.planit.travelplanner.model.SelectedPlace;
import com.trip.planit.travelplanner.model.TravelPlan;
import com.trip.planit.travelplanner.repository.SelectedPlaceRepository;
import com.trip.planit.travelplanner.repository.TravelPlanRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TravelRouteService {

    private final TravelPlanRepository travelPlanRepository;
    private final SelectedPlaceRepository selectedPlaceRepository;

    public TravelRouteService(TravelPlanRepository travelPlanRepository,
                              SelectedPlaceRepository selectedPlaceRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.selectedPlaceRepository = selectedPlaceRepository;
    }

    /**
     * 여행 계획 저장 (신규 생성 또는 수정).
     * 저장 시, destination(목적지)이 변경될 수 있으므로
     * 'placesCache'를 무효화(@CacheEvict)할 수 있다.
     */
    @Transactional
    // 만약 destination을 키로 캐싱한다면, 아래처럼 Evict
    //        destination이 null이 아닐 때만 무효화하고 싶으면 condition 사용 가능
    @CacheEvict(value = "placesCache", key = "#travelPlanDTO.destination",
            condition = "#travelPlanDTO.destination != null")
    public TravelPlanDTO saveTravelPlan(TravelPlanDTO travelPlanDTO) {
        TravelPlan travelPlan = createTravelPlan(travelPlanDTO);
        List<SelectedPlace> selectedPlaces = createSelectedPlaces(travelPlanDTO, travelPlan);
        travelPlan.setSelectedPlaces(selectedPlaces);

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);

        return convertToDTO(savedPlan);
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

    /**
     * 전체 장소 조회 (캐싱과는 무관, 필요 시 캐시 가능)
     */
//    public List<SelectedPlaceDTO> getAllPlaces() {
//        return selectedPlaceRepository.findAll().stream()
//                .map(this::convertToSelectedPlaceDTO)
//                .collect(Collectors.toList());
//    }

    /**
     * 장소 삭제 시, 코스(TravelPlan)의 경로 캐시(routeCache)를 무효화하는 것을
     * 고려해볼 수도 있다. 단, 정확한 planId + mode를 모르면 allEntries로 삭제.
     */
    @Transactional
    @CacheEvict(value = "routeCache", allEntries = true)
    // ↑ 'deleteSelectedPlace'가 특정 planId를 알고 있다면, 그에 맞춰 key 설정하는 게 좋음.
    public boolean deleteSelectedPlace(Long id) {
        if (selectedPlaceRepository.existsById(id)) {
            selectedPlaceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 전체 코스(TravelPlan) 삭제 시,
     * routeCache 및 placesCache를 무효화할 수도 있음.
     */
    @Transactional
    public boolean deleteTravelPlan(Long id) {
        if (travelPlanRepository.existsById(id)) {

            evictRouteCache(id, "driving");
            evictRouteCache(id, "transit");

            travelPlanRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * @CacheEvict를 이용하여 routeCache 특정 키 삭제
     */
    @CacheEvict(value = "routeCache", key = "#planId + '_' + #mode")
    public void evictRouteCache(Long planId, String mode) {
        // 본문은 비어 있어도, 애노테이션에 의해 캐시가 비워짐
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

    private SelectedPlaceDTO convertToSelectedPlaceDTO(SelectedPlace place) {
        return new SelectedPlaceDTO(
                place.getId(),
                place.getPlaceName(),
                place.getPlaceId(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
    }
}
