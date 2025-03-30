package com.antalyaotel.repository;

import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.Room;
import com.antalyaotel.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomer(Customer customer);
    List<Reservation> findByRoom(Room room);
    List<Reservation> findByRoomAndCheckInDateBetweenOrCheckOutDateBetween(
            Room room,
            LocalDate checkInStart, LocalDate checkInEnd,
            LocalDate checkOutStart, LocalDate checkOutEnd
    );
    boolean existsByRoomAndCheckInDateBetweenOrCheckOutDateBetween(
            Room room,
            LocalDate checkInStart, LocalDate checkInEnd,
            LocalDate checkOutStart, LocalDate checkOutEnd
    );
} 