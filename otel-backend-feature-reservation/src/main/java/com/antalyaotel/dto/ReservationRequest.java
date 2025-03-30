package com.antalyaotel.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long roomId;
    private Long customerId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private String specialRequests;
}

