package com.example.airlines.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportDTO {
    private Long airportId;
    private String airportCode;
    private String airportName;
    private String cityName;
}
