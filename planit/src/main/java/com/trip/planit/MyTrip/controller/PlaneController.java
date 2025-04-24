package com.trip.planit.MyTrip.controller;

import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.trip.planit.MyTrip.config.AmadeusClient;
import com.trip.planit.MyTrip.dto.PlaneCriteria;
import com.trip.planit.MyTrip.entity.PlaneEntity;
import com.trip.planit.MyTrip.exception.PlaneNotFoundException;
import com.trip.planit.MyTrip.service.PlaneSelector;
import com.trip.planit.MyTrip.service.PlaneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyTrip")
@RestController
@RequestMapping("/MyTrip")
@RequiredArgsConstructor
public class PlaneController {
    private final AmadeusClient amadeusClient;
    private final PlaneService planeService;
    private final PlaneSelector planeSelector;

    @PostMapping("/flights")
    @Operation(summary = "항공권 정보 저장")
    public PlaneEntity saveFlight(
            @RequestParam String origin, @RequestParam String destination,
            @RequestParam String departDate, @RequestParam String departTime,
            @RequestParam(required = false) String arrivalTime, @RequestParam String passengers)
            throws ResponseException {

        int count = Integer.parseInt(passengers);
        PlaneCriteria criteria = new PlaneCriteria();
        criteria.setOrigin(origin); criteria.setDestination(destination);
        criteria.setDepartDate(departDate); criteria.setDepartTime(departTime);
        criteria.setArrivalTime(arrivalTime); criteria.setPassengerCount(count);

        FlightOfferSearch[] offers = amadeusClient.getAmadeus().shopping.flightOffersSearch.get(
                Params.with("originLocationCode", origin)
                        .and("destinationLocationCode", destination)
                        .and("departureDate", departDate)
                        .and("adults", passengers)
                        .and("max", 5)
        );
        FlightOfferSearch offer = planeSelector.selectMatchingOffer(offers, criteria);
        if (offer == null) throw new PlaneNotFoundException("No matching flight offer found");

        PlaneEntity entity = new PlaneEntity();
        entity.setPlaneId(offer.getId());
        entity.setOrigin(origin);
        entity.setDestination(destination);
        var itin = offer.getItineraries()[0];
        entity.setDepartureDateTime(LocalDateTime.parse(itin.getSegments()[0].getDeparture().getAt()));
        entity.setArrivalDateTime(LocalDateTime.parse(itin.getSegments()[itin.getSegments().length - 1].getArrival().getAt()));
        entity.setPassengerCount(count);
        return planeService.save(entity);
    }
}