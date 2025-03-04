package com.trip.planit.MyTrip.service;

import com.amadeus.resources.FlightOfferSearch;
import com.trip.planit.MyTrip.dto.PlaneCriteria;
import org.springframework.stereotype.Component;

@Component
public class PlaneSelector {
    public FlightOfferSearch selectMatchingOffer(FlightOfferSearch[] offers, PlaneCriteria criteria) {
        for (FlightOfferSearch offer : offers)
            if (matches(offer, criteria)) return offer;
        return null;
    }
    private boolean matches(FlightOfferSearch offer, PlaneCriteria criteria) {
        if (offer.getItineraries() == null || offer.getItineraries().length == 0) return false;
        var itin = offer.getItineraries()[0];
        if (itin.getSegments() == null || itin.getSegments().length == 0) return false;
        String depAt = itin.getSegments()[0].getDeparture().getAt();
        if (!depAt.contains(criteria.getDepartTime())) return false;
        if (criteria.getArrivalTime() != null && !criteria.getArrivalTime().isEmpty()) {
            var segments = itin.getSegments();
            String arrAt = segments[segments.length - 1].getArrival().getAt();
            if (!arrAt.contains(criteria.getArrivalTime())) return false;
        }
        return true;
    }
}
