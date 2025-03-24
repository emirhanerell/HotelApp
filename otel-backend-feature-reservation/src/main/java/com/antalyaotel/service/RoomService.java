package com.antalyaotel.service;

import com.antalyaotel.model.Room;
import com.antalyaotel.repository.RoomRepository;
import com.antalyaotel.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    public Room createRoom(Room room){
    if (room.getType() == null) {
        throw new IllegalArgumentException("Room type cannot be null");
    }
        return roomRepository.save(room);
    }
    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
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


    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }


}

