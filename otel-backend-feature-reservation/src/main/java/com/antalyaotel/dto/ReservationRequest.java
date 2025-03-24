package com.antalyaotel.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ReservationRequest {
    private Long userId;
    private Long customerId;
    private Long roomNumber;
    private LocalDate startDate;
    private LocalDate endDate;
}

