package com.example.airlines.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long flightId;
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private ZonedDateTime departureTime;
    private ZonedDateTime arrivalTime;
    private Double basePrice;
    private Integer availableSeats;
    private String status;
}
