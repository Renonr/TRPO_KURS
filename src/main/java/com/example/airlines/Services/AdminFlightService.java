package com.example.airlines.Services;

import com.example.airlines.DTO.FlightCreationDTO;
import com.example.airlines.DTO.FlightDTO;
import com.example.airlines.Exception.BusinessLogicException;
import com.example.airlines.Models.Airport;
import com.example.airlines.Models.Flight;
import com.example.airlines.Repositories.AirportRepository;
import com.example.airlines.Repositories.FlightRepository;
import com.example.airlines.Repositories.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public FlightDTO createFlight(FlightCreationDTO flightCreationDTO) {
        // Проверка аэропортов
        Airport departureAirport = airportRepository.findById(flightCreationDTO.getDepartureAirportId())
                .orElseThrow(() -> new BusinessLogicException("Аэропорт вылета не найден"));

        Airport arrivalAirport = airportRepository.findById(flightCreationDTO.getArrivalAirportId())
                .orElseThrow(() -> new BusinessLogicException("Аэропорт прилета не найден"));

        // Проверка времени
        if (flightCreationDTO.getArrivalTime().isBefore(flightCreationDTO.getDepartureTime())) {
            throw new BusinessLogicException("Время прилета должно быть после времени вылета");
        }

        // Создание рейса
        Flight flight = Flight.builder()
                .flightNumber(flightCreationDTO.getFlightNumber())
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureTime(flightCreationDTO.getDepartureTime())
                .arrivalTime(flightCreationDTO.getArrivalTime())
                .basePrice(flightCreationDTO.getBasePrice())
                .availableSeats(flightCreationDTO.getTotalSeats())
                .totalSeats(flightCreationDTO.getTotalSeats())
                .status(flightCreationDTO.getStatus() != null ? flightCreationDTO.getStatus() : "SCHEDULED")
                .build();

        Flight savedFlight = flightRepository.save(flight);
        return convertToFlightDTO(savedFlight);
    }

    @Transactional
    public FlightDTO updateFlight(FlightDTO flightDTO) {
        Flight flight = flightRepository.findById(flightDTO.getFlightId())
                .orElseThrow(() -> new BusinessLogicException("Рейс не найден"));

        // Обновляем только разрешенные поля из DTO
        if (flightDTO.getFlightNumber() != null) {
            flight.setFlightNumber(flightDTO.getFlightNumber());
        }

        if (flightDTO.getDepartureTime() != null) {
            flight.setDepartureTime(flightDTO.getDepartureTime());
        }

        if (flightDTO.getArrivalTime() != null) {
            flight.setArrivalTime(flightDTO.getArrivalTime());
        }

        if (flightDTO.getBasePrice() != null) {
            flight.setBasePrice(flightDTO.getBasePrice());
        }

        if (flightDTO.getAvailableSeats() != null) {
            // Проверка, что доступных мест не больше общего количества
            if (flightDTO.getAvailableSeats() > flight.getTotalSeats()) {
                throw new BusinessLogicException("Доступных мест не может быть больше общего количества");
            }
            flight.setAvailableSeats(flightDTO.getAvailableSeats());
        }

        if (flightDTO.getStatus() != null && !flightDTO.getStatus().isEmpty()) {
            // Можно добавить валидацию допустимых статусов
            if (!List.of("SCHEDULED", "CANCELLED", "COMPLETED").contains(flightDTO.getStatus())) {
                throw new BusinessLogicException("Недопустимый статус рейса");
            }
            flight.setStatus(flightDTO.getStatus());
        }

        Flight updatedFlight = flightRepository.save(flight);
        return convertToFlightDTO(updatedFlight);
    }

    @Transactional
    public void deleteFlight(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new BusinessLogicException("Рейс не найден"));

        if (ticketRepository.existsByFlight(flight)) {
            throw new BusinessLogicException("Невозможно удалить рейс с купленными билетами");
        }

        flightRepository.delete(flight);
    }

    private FlightDTO convertToFlightDTO(Flight flight) {
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
