package com.antalyaotel.service;

import com.antalyaotel.model.*;
import com.antalyaotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;

    public Reservation createReservation(Reservation reservation) {
        try {
            logger.info("Creating reservation: {}", reservation);
            
            // Oda kontrolü
            Room room = roomRepository.findById(reservation.getRoom().getId())
                    .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + reservation.getRoom().getId()));
            
            // Müşteri kontrolü
            Customer customer = customerRepository.findById(reservation.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı: " + reservation.getCustomer().getId()));
            
            // Misafir sayısı kontrolü
            if (reservation.getNumberOfGuests() > room.getCapacity()) {
                throw new RuntimeException("Seçilen oda için belirtilen misafir sayısı çok fazla");
            }
            
            // Oda müsaitlik kontrolü
            if (!isRoomAvailable(room, reservation.getCheckInDate(), reservation.getCheckOutDate())) {
                throw new RuntimeException("Seçilen tarihler için oda müsait değil");
            }
            
            // Toplam fiyat hesaplama
            long days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
            double totalPrice = room.getPrice() * days;
            
            // Rezervasyon nesnesini güncelle
            reservation.setRoom(room);
            reservation.setCustomer(customer);
            reservation.setTotalPrice(totalPrice);
            reservation.setStatus(ReservationStatus.PENDING);
            
            // Rezervasyonu kaydet
            Reservation savedReservation = reservationRepository.save(reservation);
            logger.info("Reservation created successfully: {}", savedReservation.getId());
            
            return savedReservation;
        } catch (Exception e) {
            logger.error("Error creating reservation: ", e);
            throw new RuntimeException("Rezervasyon oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    public List<Reservation> getCustomerReservations(Long customerId) {
        try {
            logger.info("Fetching reservations for customer: {}", customerId);
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı: " + customerId));
            List<Reservation> reservations = reservationRepository.findByCustomer(customer);
            logger.info("Found {} reservations for customer {}", reservations.size(), customerId);
            return reservations;
        } catch (Exception e) {
            logger.error("Error fetching customer reservations: ", e);
            throw new RuntimeException("Rezervasyonlar yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    public Reservation getReservationById(Long id) {
        try {
            logger.info("Fetching reservation with id: {}", id);
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı: " + id));
            logger.info("Found reservation: {}", reservation.getId());
            return reservation;
        } catch (Exception e) {
            logger.error("Error fetching reservation: ", e);
            throw new RuntimeException("Rezervasyon yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    public Reservation updateReservationStatus(Long id, ReservationStatus status) {
        try {
            logger.info("Updating reservation status: id={}, status={}", id, status);
            Reservation reservation = getReservationById(id);
            reservation.setStatus(status);
            Reservation updatedReservation = reservationRepository.save(reservation);
            logger.info("Reservation status updated successfully: {}", updatedReservation.getId());
            return updatedReservation;
        } catch (Exception e) {
            logger.error("Error updating reservation status: ", e);
            throw new RuntimeException("Rezervasyon durumu güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    private boolean isRoomAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            boolean isAvailable = !reservationRepository.existsByRoomAndCheckInDateBetweenOrCheckOutDateBetween(
                    room, checkInDate, checkOutDate, checkInDate, checkOutDate);
            logger.info("Room {} availability check: {}", room.getId(), isAvailable);
            return isAvailable;
        } catch (Exception e) {
            logger.error("Error checking room availability: ", e);
            throw new RuntimeException("Oda müsaitlik kontrolü yapılırken bir hata oluştu: " + e.getMessage());
        }
    }
} 