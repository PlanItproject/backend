package com.trip.planit.travelplanner.controller;

import com.trip.planit.travelplanner.dto.SelectedPlaceDTO;
import com.trip.planit.travelplanner.dto.TravelPlanDTO;
import com.trip.planit.travelplanner.model.TravelPlan;
import com.trip.planit.travelplanner.repository.TravelPlanRepository;
import com.trip.planit.travelplanner.service.GoogleApiService;
import com.trip.planit.travelplanner.service.TravelRouteService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travel-courses")
public class TravelPlanController {

    private final TravelRouteService travelRouteService;
    private final GoogleApiService googleApiService;
    private final TravelPlanRepository travelPlanRepository;


    public TravelPlanController(TravelRouteService travelRouteService, GoogleApiService googleApiService,
                                TravelPlanRepository travelPlanRepository) {
        this.travelRouteService = travelRouteService;
        this.googleApiService = googleApiService;
        this.travelPlanRepository = travelPlanRepository;
    }

    // 지역 별 여행지 목록 조회
    @GetMapping("/places")
    public ResponseEntity<List<SelectedPlaceDTO>> getPlaces(@RequestParam String destination) {
        List<SelectedPlaceDTO> places = googleApiService.getPlaces(destination);
        return ResponseEntity.ok(places);
    }

//    findAll()을 사용해서 구현했으나 findById()가 더 정확하다 판단해서 다른 사용처가 있을지 고민 중
//    @GetMapping("/places/all")
//    public ResponseEntity<List<SelectedPlaceDTO>> getAllPlaces() {
//        List<SelectedPlaceDTO> allPlaces = travelPlanService.getAllPlaces();
//        return ResponseEntity.ok(allPlaces);
//    }

    //  전체 여행 코스 리스트 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TravelPlan>> getUserTravelPlans(@PathVariable Long userId) {
        List<TravelPlan> plans = travelPlanRepository.findByUserId(userId);
        return ResponseEntity.ok(plans);
    }


    // 여행 계획 추가
    @PostMapping
    public ResponseEntity<TravelPlanDTO> createTravelPlan(@RequestBody TravelPlanDTO travelPlanDTO) {
        TravelPlanDTO savedPlanDTO = travelRouteService.saveTravelPlan(travelPlanDTO);
        return ResponseEntity.ok(savedPlanDTO);
    }

    // 여행 계획 삭제
    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> deleteSelectedPlace(@PathVariable Long id) {
        boolean isDeleted = travelRouteService.deleteSelectedPlace(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 여행 계획 전체 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTravelPlan(@PathVariable Long id) {
        boolean isDeleted = travelRouteService.deleteTravelPlan(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}