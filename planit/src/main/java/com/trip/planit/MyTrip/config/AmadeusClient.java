package com.trip.planit.MyTrip.config;

import com.amadeus.Amadeus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmadeusClient {
    private final Amadeus amadeus;

    public AmadeusClient(@Value("${amadeus.clientId}") String clientId,
                         @Value("${amadeus.clientSecret}") String clientSecret) {
        this.amadeus = Amadeus.builder(clientId, clientSecret).build();
    }

    public Amadeus getAmadeus() {
        return amadeus;
    }
}
