package com.antalyaotel.controller;

import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.User;
import com.antalyaotel.repository.UserRepository;
import com.antalyaotel.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    // 📌 1️⃣ Rezervasyonu ONAYLAMA
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<?> confirmReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails adminDetails) {

        User admin = userRepository.findByEmail(adminDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        try {
            Reservation confirmedReservation = reservationService.confirmReservation(reservationId, admin.getId());
            return ResponseEntity.ok(confirmedReservation);
        } catch (RuntimeException | IOException e) {
            // 🔥 Alternatif tarihleri önerelim
            List<LocalDate> availableDates = reservationService.findAvailableDates(reservationId);
            return ResponseEntity.badRequest().body("This room is no longer available. Suggested dates: " + availableDates);
        }
    }

    // 📌 2️⃣ Rezervasyonu İPTAL ETME
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Reservation> cancelReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails adminDetails) {

        // 🔹 Admin bilgilerini JWT Token'dan al
        User admin = userRepository.findByEmail(adminDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // 🔹 Admin ID'yi request'ten almak yerine, JWT'den gelen admin.getId() kullanılıyor
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId, admin.getId()));
    }


    // 📌 3️⃣ Tüm rezervasyonları listeleme (Admin)
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
}
