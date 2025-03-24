package com.antalyaotel.controller;

import com.antalyaotel.model.Reservation;
import com.antalyaotel.repository.ReservationRepository;
import com.antalyaotel.service.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j // 🛠 Loglama için
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final ReservationRepository reservationRepository;

    /**
     * 📌 Google Calendar'a rezervasyon ekleme
     */
    @PostMapping("/add")
    public ResponseEntity<String> addEvent(@RequestParam Long reservationId) {
        try {
            // 📌 1️⃣ Rezervasyonu Veritabanından Çek
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı."));

            // 📌 2️⃣ Oda Bilgisinin Eksik Olmadığını Kontrol Et
            if (reservation.getRoom() == null) {
                log.error("Rezervasyon {} için oda bilgisi eksik!", reservationId);
                return ResponseEntity.badRequest().body("Hata: Rezervasyonun oda bilgisi eksik.");
            }

            // 📌 3️⃣ Google Calendar'a Ekleyelim
            googleCalendarService.addEventToCalendar(reservation); // GoogleCalendarService'e gönderiliyor

            return ResponseEntity.ok("Rezervasyon Google Calendar'a başarıyla eklendi!");
        } catch (Exception e) {
            log.error("Google Calendar'a eklenirken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Google Calendar'a eklenirken hata oluştu: " + e.getMessage());
        }
    }
}
