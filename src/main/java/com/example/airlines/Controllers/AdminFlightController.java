package com.example.airlines.Controllers;

import com.example.airlines.DTO.FlightCreationDTO;
import com.example.airlines.DTO.FlightDTO;
import com.example.airlines.Services.AdminFlightService;
import com.example.airlines.Services.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/flights")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFlightController {
    private final AdminFlightService adminFlightService;
    private final FlightService flightService;

    public AdminFlightController(AdminFlightService adminFlightService, FlightService flightService) {
        this.adminFlightService = adminFlightService;
        this.flightService = flightService;
    }

    @GetMapping
    public List<FlightDTO> getAllFlights(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction
    ) {
        return flightService.getAllFlights(sortBy, direction);
    }

    @PostMapping
    public ResponseEntity<FlightDTO> createFlight(@Valid @RequestBody FlightCreationDTO flightCreationDTO) {
        FlightDTO createdFlight = adminFlightService.createFlight(flightCreationDTO);
        return ResponseEntity.ok(createdFlight);
    }

    @PutMapping("/{flightId}")
    public ResponseEntity<FlightDTO> updateFlight(
            @PathVariable Long flightId,
            @Valid @RequestBody FlightDTO flightDTO) {
        flightDTO.setFlightId(flightId);
        FlightDTO updatedFlight = adminFlightService.updateFlight(flightDTO);
        return ResponseEntity.ok(updatedFlight);
    }

    @DeleteMapping("/{flightId}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long flightId) {
        adminFlightService.deleteFlight(flightId);
        return ResponseEntity.noContent().build();
    }
}
