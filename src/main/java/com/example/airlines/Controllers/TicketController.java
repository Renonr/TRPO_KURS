package com.example.airlines.Controllers;

import com.example.airlines.DTO.TicketDTO;
import com.example.airlines.Exception.BusinessLogicException;
import com.example.airlines.Models.User;
import com.example.airlines.Services.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Покупка билета
    @PostMapping("/purchase")
    public ResponseEntity<TicketDTO> purchaseTicket(
            @AuthenticationPrincipal User user,
            @RequestParam Long flightId) {
        try {
            TicketDTO ticket = ticketService.purchaseTicket(user.getUserId(), flightId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Получение билетов пользователя
    @GetMapping("/my")
    public ResponseEntity<List<TicketDTO>> getUserTickets(@AuthenticationPrincipal User user) {
        List<TicketDTO> tickets = ticketService.getTicketsByUser(user);
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> cancelTicket(
            @AuthenticationPrincipal User user,
            @PathVariable Long ticketId) {
        try {
            ticketService.cancelTicket(user.getUserId(), ticketId);
            return ResponseEntity.noContent().build();
        } catch (BusinessLogicException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
