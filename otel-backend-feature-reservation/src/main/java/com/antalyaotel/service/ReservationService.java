package com.antalyaotel.service;

import com.antalyaotel.enums.ReservationStatus;
import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.Room;
import com.antalyaotel.model.User;
import com.antalyaotel.model.Customer;
import com.antalyaotel.repository.ReservationRepository;
import com.antalyaotel.repository.RoomRepository;
import com.antalyaotel.repository.UserRepository;
import com.antalyaotel.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final GoogleCalendarService googleCalendarService;

    // 📌 1️⃣ Rezervasyon oluşturma işlemi
    public Reservation createReservation(Long userId, Long customerId, Long roomId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 🔥 Oda doluluk kontrolü
        boolean isRoomAvailable = !reservationRepository.existsOverlappingReservation(roomId, startDate, endDate);
        if (!isRoomAvailable) {
            throw new RuntimeException("This room is already booked for the selected dates.");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .customer(customer)
                .room(room)
                .startDate(startDate)
                .endDate(endDate)
                .status(ReservationStatus.PENDING) // Varsayılan olarak PENDING
                .build();

        reservationRepository.save(reservation);
        emailService.sendReservationConfirmation(user.getEmail(), reservation);

        return reservation;
    }

    // 📌 2️⃣ Kullanıcının rezervasyonlarını listeleme
    public List<Reservation> getUserReservations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUser(user);
    }

    // 📌 3️⃣ Tüm rezervasyonları listeleme (Admin yetkisi ile)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // 📌 4️⃣ Rezervasyonu güncelleme
    public Reservation updateReservation(Long reservationId, LocalDate startDate, LocalDate endDate, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 🔥 Tarih değiştiyse, çakışma kontrolü yap
        if (!reservation.getStartDate().equals(startDate) || !reservation.getEndDate().equals(endDate)) {
            boolean isRoomAvailable = reservationRepository
                    .findByRoomAndStartDateLessThanEqualAndEndDateGreaterThanEqual(reservation.getRoom(), endDate, startDate)
                    .isEmpty();

            if (!isRoomAvailable) {
                throw new RuntimeException("This room is already booked for the selected dates.");
            }
        }

        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    // 📌 5️⃣ Rezervasyonu silme
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservationRepository.delete(reservation);
    }
    private void removePendingConflictingReservations(Reservation confirmedReservation) {
        List<Reservation> conflictingReservations = reservationRepository.findByRoomAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                confirmedReservation.getRoom(),
                confirmedReservation.getEndDate(),
                confirmedReservation.getStartDate()
        );

        List<Reservation> pendingReservations = conflictingReservations.stream()
                .filter(res -> res.getStatus() == ReservationStatus.PENDING)
                .collect(Collectors.toList());

        reservationRepository.deleteAll(pendingReservations);
    }

    // 📌 6️⃣ Rezervasyonu ONAYLAMA (Admin)
    public Reservation confirmReservation(Long reservationId, Long adminId)throws IOException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            throw new RuntimeException("Only PENDING reservations can be confirmed.");
        }

        boolean isRoomAvailable = !reservationRepository.existsOverlappingReservation(
                reservation.getRoom().getId(),
                reservation.getStartDate(),
                reservation.getEndDate());

        if (!isRoomAvailable) {
            throw new RuntimeException("This room is no longer available for the selected dates.");
        }

        removePendingConflictingReservations(reservation);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setApprovedByAdmin(admin);
        reservationRepository.save(reservation);

        // 📧 Kullanıcıya bildirim gönder
        emailService.sendEmail(reservation.getUser().getEmail(),
                "Rezervasyon Onaylandı ✅",
                "Sayın " + reservation.getUser().getName() + ", rezervasyonunuz onaylandı!\n" +
                        "Oda No: " + reservation.getRoom().getRoomNumber() + "\n" +
                        "Giriş: " + reservation.getStartDate() + "\n" +
                        "Çıkış: " + reservation.getEndDate());

        // 🔥 Google Calendar'a ekleme
        googleCalendarService.addEventToCalendar(reservation);

        return reservation;
    }

    public List<Reservation> getReservationsByRoomType(String roomType) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getRoom().getType().equalsIgnoreCase(roomType))
                .collect(Collectors.toList());
    }


    // 📌 7️⃣ Rezervasyonu İPTAL ETME (Admin)
    public Reservation cancelReservation(Long reservationId, Long adminId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            throw new RuntimeException("Only PENDING reservations can be canceled.");
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setApprovedByAdmin(userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found")));

        reservationRepository.save(reservation);

        // 📧 Kullanıcıya e-posta bildirimi
        emailService.sendEmail(reservation.getUser().getEmail(),
                "Rezervasyon İptal Edildi ❌",
                "Sayın " + reservation.getUser().getName() + ", rezervasyonunuz iptal edilmiştir.\n" +
                        "Oda No: " + reservation.getRoom().getRoomNumber());

        return reservation;
    }

    // 📌 8️⃣ Alternatif Tarihleri Bulma
    public List<LocalDate> findAvailableDates(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        List<Reservation> reservations = reservationRepository
                .findByRoomAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        reservation.getRoom(),
                        reservation.getEndDate(),
                        reservation.getStartDate()
                );

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate tempDate = reservation.getStartDate();

        while (!tempDate.isAfter(reservation.getEndDate())) {
            LocalDate finalTempDate = tempDate;  // Lambda içinde kullanmak için kopya değişken oluştur

            boolean isAvailable = reservations.stream().noneMatch(r ->
                    !finalTempDate.isBefore(r.getStartDate()) && !finalTempDate.isAfter(r.getEndDate()));

            if (isAvailable) {
                availableDates.add(tempDate);
            }
            tempDate = tempDate.plusDays(1);
        }

        return availableDates;
    }
}
