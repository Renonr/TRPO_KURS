package com.example.airlines.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "airports")
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long airportId;

    @Column(unique = true, nullable = false)
    private String airportCode;

    @Column(nullable = false)
    private String airportName;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @OneToMany(mappedBy = "departureAirport")
    private List<Flight> departingFlights;

    @OneToMany(mappedBy = "arrivalAirport")
    private List<Flight> arrivingFlights;
}
