package com.example.airlines.Controllers;

import com.example.airlines.DTO.AirportDTO;
import com.example.airlines.Models.Airport;
import com.example.airlines.Repositories.AirportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/airports")
public class AirportController {
    private final AirportRepository airportRepository;

    public AirportController(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @GetMapping
    public ResponseEntity<List<AirportDTO>> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        List<AirportDTO> airportDTOs = airports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(airportDTOs);
    }

    private AirportDTO convertToDTO(Airport airport) {
        return AirportDTO.builder()
                .airportId(airport.getAirportId())
                .airportCode(airport.getAirportCode())
                .cityName(airport.getCity().getCityName())
                .build();
    }
}
