package com.antalyaotel.controller;

import com.antalyaotel.model.Room;
import com.antalyaotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    @Autowired
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        if (room.getType() == null || room.getType().isEmpty()) {
            return ResponseEntity.badRequest().body("Room type is required");
        }
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.ok(createdRoom);
    }


    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        return roomService.getRoomById(id)
                .map(existingRoom -> {
                    existingRoom.setRoomNumber(roomDetails.getRoomNumber());
                    existingRoom.setType(roomDetails.getType());
                    existingRoom.setPrice(roomDetails.getPrice());
                    existingRoom.setIsAvailable(roomDetails.getIsAvailable());

                    Room updatedRoom = roomService.updateRoom(existingRoom);
                    return ResponseEntity.ok(updatedRoom);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(room -> {
                    roomService.deleteRoom(id);
                    return ResponseEntity.ok("Room deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
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

