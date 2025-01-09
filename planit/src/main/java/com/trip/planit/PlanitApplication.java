package com.trip.planit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PlanitApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanitApplication.class, args);
	}

}
