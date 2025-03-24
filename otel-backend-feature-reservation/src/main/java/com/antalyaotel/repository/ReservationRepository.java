package com.antalyaotel.repository;

import com.antalyaotel.model.Reservation;
import com.antalyaotel.model.Room;
import com.antalyaotel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

//@Repository
//public interface ReservationRepository extends JpaRepository<Reservation, Long> {
//    List<Reservation> findByUserId(Long userId);
//    List<Reservation> findByRoomIdAndStartDateBetween(Long roomId, LocalDate start, LocalDate end);
//}
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByRoomAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Room room, LocalDate start, LocalDate end);

    @Query("""
    SELECT COUNT(r) > 0 FROM Reservation r 
    WHERE r.room.id = :roomId 
    AND r.status = 'CONFIRMED'
    AND (:startDate BETWEEN r.startDate AND r.endDate 
    OR :endDate BETWEEN r.startDate AND r.endDate 
    OR (r.startDate BETWEEN :startDate AND :endDate))
""")
    boolean existsOverlappingReservation(@Param("roomId") Long roomId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

}

