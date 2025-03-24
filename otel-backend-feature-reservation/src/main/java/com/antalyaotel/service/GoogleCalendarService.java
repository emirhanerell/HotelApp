package com.antalyaotel.service;

import com.antalyaotel.model.Reservation;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.TimeZone;

@Service
@Slf4j // 📌 Log ekledim
public class GoogleCalendarService {

    private final Calendar calendarService;

    // 📌 Takvim ID'sini buraya ekledim
    private static final String CALENDAR_ID = "1e572ac7375fb042b0e539f159750ca6d9f053c9fb6c5ddf115cc8534611f6ab@group.calendar.google.com";

    @Autowired
    public GoogleCalendarService(Calendar calendarService) {
        this.calendarService = calendarService;
    }

    public void addEventToCalendar(Reservation reservation) {
        try {
            Event event = new Event()
                    .setSummary("Otel Rezervasyonu: " + reservation.getRoom().getRoomNumber())
                    .setDescription("Müşteri: " + reservation.getUser().getName());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(reservation.getStartDate().toString() + "T12:00:00Z"))
                    .setTimeZone(TimeZone.getDefault().getID());

            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(reservation.getEndDate().toString() + "T12:00:00Z"))
                    .setTimeZone(TimeZone.getDefault().getID());

            event.setStart(start);
            event.setEnd(end);

            // 📌 Etkinliği eklerken log ekledim
            log.info("Google Calendar'a etkinlik ekleniyor: {}", event);

            calendarService.events().insert(CALENDAR_ID, event).execute();

            log.info("Etkinlik başarıyla eklendi: {} -> {}", reservation.getRoom().getRoomNumber(), CALENDAR_ID);
        } catch (IOException e) {
            log.error("Google Calendar'a eklenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}
