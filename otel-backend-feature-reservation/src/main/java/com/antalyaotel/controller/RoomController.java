package com.antalyaotel.controller;

import com.antalyaotel.model.Room;
import com.antalyaotel.service.RoomService;
import com.antalyaotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class RoomController {
    private final RoomService roomService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Room>> getAllRooms() {
        try {
            logger.info("Tüm odalar getiriliyor...");
            List<Room> rooms = roomService.getAllRooms();
            logger.info("{} oda başarıyla getirildi.", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            logger.error("Odalar getirilirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> addRoom(@RequestBody Room room, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            logger.info("Yeni oda ekleniyor: {}", room.getRoomNumber());
            Room savedRoom = roomService.addRoom(room, email);
            logger.info("Oda başarıyla eklendi: {}", savedRoom.getRoomNumber());
            return ResponseEntity.ok(savedRoom);
        } catch (Exception e) {
            logger.error("Oda eklenirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        try {
            logger.info("Oda getiriliyor, ID: {}", id);
            Room room = roomService.getRoomById(id);
            logger.info("Oda başarıyla getirildi: {}", room.getRoomNumber());
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            logger.error("Oda getirilirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            logger.info("Oda güncelleniyor, ID: {}", id);
            Room updatedRoom = roomService.updateRoom(id, room);
            logger.info("Oda başarıyla güncellendi: {}", updatedRoom.getRoomNumber());
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            logger.error("Oda güncellenirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            logger.info("Oda siliniyor, ID: {}", id);
            roomService.deleteRoom(id);
            logger.info("Oda başarıyla silindi, ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Oda silinirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Room>> getRoomsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<Room> rooms = roomService.getRoomsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/filtered") // 🟢 Yeni URL ekleyerek çakışmayı önledik!
    public List<Room> getRooms(
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        return roomService.getFilteredRooms(priceMin, priceMax, type, available, sortBy, sortOrder);
    }
}

