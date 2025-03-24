package com.antalyaotel.controller;

import com.antalyaotel.dto.ReservationRequest;
import com.antalyaotel.enums.ReservationStatus;
import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.User;
import com.antalyaotel.repository.UserRepository;
import com.antalyaotel.service.EmailService;
import com.antalyaotel.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(
                request.getUserId(),
                request.getCustomerId(),
                request.getRoomNumber(),
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getUserReservations(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Reservation>> filterReservationsByRoomType(@RequestParam String roomType) {
        return ResponseEntity.ok(reservationService.getReservationsByRoomType(roomType));
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long reservationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam ReservationStatus status) {
        Reservation updatedReservation = reservationService.updateReservation(reservationId, startDate, endDate, status);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/reservations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Reservation>> getUserReservations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(reservationService.getUserReservations(user.getId()));
    }
}
