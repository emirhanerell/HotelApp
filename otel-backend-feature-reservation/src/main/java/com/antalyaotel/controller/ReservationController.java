package com.antalyaotel.controller;

import com.antalyaotel.dto.ErrorResponse;
import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.ReservationStatus;
import com.antalyaotel.model.Room;
import com.antalyaotel.service.ReservationService;
import com.antalyaotel.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReservationController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        try {
            logger.info("Creating new reservation: {}", reservation);
            Reservation createdReservation = reservationService.createReservation(reservation);
            return ResponseEntity.ok(createdReservation);
        } catch (Exception e) {
            logger.error("Error creating reservation: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Rezervasyon oluşturulurken bir hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerReservations(@PathVariable Long customerId) {
        try {
            logger.info("Fetching reservations for customer: {}", customerId);
            List<Reservation> reservations = reservationService.getCustomerReservations(customerId);
            if (reservations == null || reservations.isEmpty()) {
                return ResponseEntity.ok().body(reservations);
            }
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            logger.error("Error fetching customer reservations: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Rezervasyonlar yüklenirken bir hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            logger.info("Fetching reservation with id: {}", id);
            Reservation reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            logger.error("Error fetching reservation: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Rezervasyon yüklenirken bir hata oluştu: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {
        try {
            logger.info("Updating reservation status: id={}, status={}", id, status);
            Reservation updatedReservation = reservationService.updateReservationStatus(id, status);
            return ResponseEntity.ok(updatedReservation);
        } catch (Exception e) {
            logger.error("Error updating reservation status: ", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Rezervasyon durumu güncellenirken bir hata oluştu: " + e.getMessage()));
        }
    }
} 