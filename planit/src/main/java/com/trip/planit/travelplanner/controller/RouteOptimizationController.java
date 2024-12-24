package com.trip.planit.travelplanner.controller;

import com.trip.planit.travelplanner.service.RouteOptimizationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/travel-courses")
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    public RouteOptimizationController(RouteOptimizationService routeOptimizationService) {
        this.routeOptimizationService = routeOptimizationService;
    }

    @GetMapping("/{id}/routes")
    public ResponseEntity<List<String>> getOptimizedRoute(@PathVariable Long id, @RequestParam String mode) {
        List<String> routes = routeOptimizationService.calculateOptimizedRoute(id, mode);
        return ResponseEntity.ok(routes);
    }
}