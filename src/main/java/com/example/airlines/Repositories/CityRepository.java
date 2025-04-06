package com.example.airlines.Repositories;

import com.example.airlines.Models.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountry(String country);
    List<City> findByCityNameContainingIgnoreCase(String cityName);
}
