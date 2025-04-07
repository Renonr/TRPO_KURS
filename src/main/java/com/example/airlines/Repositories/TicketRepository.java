package com.example.airlines.Repositories;

import com.example.airlines.Models.Flight;
import com.example.airlines.Models.Ticket;
import com.example.airlines.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUser(User user);
    boolean existsByUserAndFlight(User user, Flight flight);

    boolean existsByFlight(Flight flight);

    boolean existsByTicketIdAndUserUserId(Long ticketId, Long userId);
}
