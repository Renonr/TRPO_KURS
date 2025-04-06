package com.example.airlines.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightCreationDTO {
    @NotBlank
    private String flightNumber;

    @NotNull
    private Long departureAirportId;

    @NotNull
    private Long arrivalAirportId;

    @NotNull
    @Future
    private ZonedDateTime departureTime;

    @NotNull
    @Future
    private ZonedDateTime arrivalTime;

    @NotNull
    @Positive
    private Double basePrice;

    @NotNull
    @Positive
    private Integer totalSeats;

    private String status;
}
