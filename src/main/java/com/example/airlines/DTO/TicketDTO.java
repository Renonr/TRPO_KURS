package com.example.airlines.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long ticketId;
    private String ticketNumber;
    private Long flightId;
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private Double price;
    private ZonedDateTime departureTime;
    private ZonedDateTime arrivalTime;
}
