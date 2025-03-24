package com.antalyaotel.specification;
import org.springframework.data.jpa.domain.Specification;
import com.antalyaotel.model.Room;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<Room> filterRooms(Double priceMin, Double priceMax, String type, Boolean isAvailable ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (priceMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceMax));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (isAvailable  != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable "), isAvailable ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

