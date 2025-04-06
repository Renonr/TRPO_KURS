package com.example.airlines.Controllers;

import com.example.airlines.DTO.FlightDTO;
import com.example.airlines.Services.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    public List<FlightDTO> getAllFlights(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction
    ) {
        return flightService.getAllFlights(sortBy, direction);
    }

    @GetMapping("/search")
    public List<FlightDTO> searchFlights(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction
    ) {
        return flightService.searchFlights(from, to, sortBy, direction);
    }
}
