package com.example.airlines.Repositories;

import com.example.airlines.Models.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
}
