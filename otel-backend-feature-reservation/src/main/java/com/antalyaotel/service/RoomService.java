package com.antalyaotel.service;

import com.antalyaotel.model.Admin;
import com.antalyaotel.model.Room;
import com.antalyaotel.repository.AdminRepository;
import com.antalyaotel.repository.RoomRepository;
import com.antalyaotel.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final RoomRepository roomRepository;
    private final AdminRepository adminRepository;
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    public Room addRoom(Room room, String adminEmail) {
        try {
            logger.info("Oda ekleme işlemi başlatıldı: {}", room.getRoomNumber());
            
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                String errorMsg = "Bu oda numarası zaten kullanılıyor: " + room.getRoomNumber();
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            Admin admin = adminRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> {
                        String errorMsg = "Admin bulunamadı: " + adminEmail;
                        logger.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });
            
            room.setAdmin(admin);
            Room savedRoom = roomRepository.save(room);
            logger.info("Oda başarıyla eklendi: {}", savedRoom.getRoomNumber());
            return savedRoom;
        } catch (Exception e) {
            logger.error("Oda eklenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    public List<Room> getAllRooms() {
        try {
            logger.info("Tüm odalar getiriliyor...");
            List<Room> rooms = roomRepository.findAll();
            logger.info("{} oda başarıyla getirildi.", rooms.size());
            return rooms;
        } catch (Exception e) {
            logger.error("Odalar getirilirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    public Room getRoomById(Long id) {
        try {
            logger.info("Oda getiriliyor, ID: {}", id);
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> {
                        String errorMsg = "Oda bulunamadı: " + id;
                        logger.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });
            logger.info("Oda başarıyla getirildi: {}", room.getRoomNumber());
            return room;
        } catch (Exception e) {
            logger.error("Oda getirilirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    public Room updateRoom(Long id, Room roomDetails) {
        try {
            logger.info("Oda güncelleniyor, ID: {}", id);
            Room room = getRoomById(id);
            
            room.setRoomNumber(roomDetails.getRoomNumber());
            room.setRoomType(roomDetails.getRoomType());
            room.setPrice(roomDetails.getPrice());
            room.setCapacity(roomDetails.getCapacity());
            
            Room updatedRoom = roomRepository.save(room);
            logger.info("Oda başarıyla güncellendi: {}", updatedRoom.getRoomNumber());
            return updatedRoom;
        } catch (Exception e) {
            logger.error("Oda güncellenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteRoom(Long id) {
        try {
            logger.info("Oda siliniyor, ID: {}", id);
            Room room = getRoomById(id);
            roomRepository.delete(room);
            logger.info("Oda başarıyla silindi, ID: {}", id);
        } catch (Exception e) {
            logger.error("Oda silinirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    public List<Room> getRoomsByPriceRange(Double minPrice, Double maxPrice) {
        return roomRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Room> getFilteredRooms(Double priceMin, Double priceMax, String type, Boolean available, String sortBy, String sortOrder) {
        Specification<Room> spec = RoomSpecification.filterRooms(priceMin, priceMax, type, available);

        Sort sort;
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = (sortOrder != null && sortOrder.equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.ASC, "id"); // Varsayılan olarak id'ye göre sırala
        }

        return roomRepository.findAll(spec, sort);
    }
}

