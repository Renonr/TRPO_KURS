package com.example.airlines.Services;

import com.example.airlines.DTO.TicketDTO;
import com.example.airlines.Exception.BusinessLogicException;
import com.example.airlines.Models.Flight;
import com.example.airlines.Models.Ticket;
import com.example.airlines.Models.User;
import com.example.airlines.Repositories.FlightRepository;
import com.example.airlines.Repositories.TicketRepository;
import com.example.airlines.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;

    // Получить все билеты пользователя
    public List<TicketDTO> getTicketsByUser(User user) {
        List<Ticket> tickets = ticketRepository.findByUser(user);
        return tickets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketDTO purchaseTicket(Long userId, Long flightId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("Пользователь не найден"));

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new BusinessLogicException("Рейс не найден"));

        if (ticketRepository.existsByUserAndFlight(user, flight)) {
            throw new BusinessLogicException("У вас уже есть билет на этот рейс");
        }

        if (flight.getAvailableSeats() <= 0) {
            throw new BusinessLogicException("На рейсе нет свободных мест");
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setUser(user);
        ticket.setFlight(flight);
        ticket.setPrice(flight.getBasePrice());

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    @Transactional
    public void cancelTicket(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessLogicException("Билет не найден"));

        if (!ticket.getUser().getUserId().equals(userId)) {
            throw new BusinessLogicException("Этот билет принадлежит другому пользователю");
        }

        Flight flight = ticket.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);

        ticketRepository.delete(ticket);
    }

    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .ticketNumber(ticket.getTicketNumber())
                .flightId(ticket.getFlight().getFlightId())
                .flightNumber(ticket.getFlight().getFlightNumber())
                .departureCity(ticket.getFlight().getDepartureAirport().getCity().getCityName())
                .arrivalCity(ticket.getFlight().getArrivalAirport().getCity().getCityName())
                .price(ticket.getPrice())
                .departureTime(ticket.getFlight().getDepartureTime())
                .arrivalTime(ticket.getFlight().getArrivalTime())
                .build();
    }
}
