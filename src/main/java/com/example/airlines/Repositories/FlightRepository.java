package com.example.airlines.Repositories;

import com.example.airlines.Models.Flight;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepartureAirport_City_CityNameAndArrivalAirport_City_CityName(
            String departureCity,
            String arrivalCity,
            Sort sort
    );

    List<Flight> findAll(Sort sort);

    boolean existsByFlightNumber(String flightNumber);
}
