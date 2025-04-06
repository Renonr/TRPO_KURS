package com.example.airlines.Services;

import com.example.airlines.DTO.FlightDTO;
import com.example.airlines.Models.Flight;
import com.example.airlines.Repositories.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightRepository flightRepository;

    public List<FlightDTO> getAllFlights(String sortBy, String direction) {
        Sort sort = buildSort(sortBy, direction);
        return flightRepository.findAll(sort)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FlightDTO> searchFlights(
            String departureCity,
            String arrivalCity,
            String sortBy,
            String direction
    ) {
        Sort sort = buildSort(sortBy, direction);
        return flightRepository
                .findByDepartureAirport_City_CityNameAndArrivalAirport_City_CityName(
                        departureCity,
                        arrivalCity,
                        sort
                )
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Sort buildSort(String sortBy, String direction) {
        if (sortBy == null) {
            sortBy = "departureTime"; // Сортировка по умолчанию
        }
        if (direction == null || !direction.equalsIgnoreCase("desc")) {
            return Sort.by(Sort.Direction.ASC, sortBy);
        }
        return Sort.by(Sort.Direction.DESC, sortBy);
    }

    private FlightDTO convertToDTO(Flight flight) {
        return FlightDTO.builder()
                .flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber())
                .departureCity(flight.getDepartureAirport().getCity().getCityName())
                .arrivalCity(flight.getArrivalAirport().getCity().getCityName())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .basePrice(flight.getBasePrice())
                .availableSeats(flight.getAvailableSeats())
                .status(flight.getStatus())
                .build();
    }
}
