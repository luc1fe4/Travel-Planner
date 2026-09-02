package com.travelplanner.packing.repository;

import com.travelplanner.packing.entity.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {
    List<PackingItem> findByTripIdOrderByIdAsc(Long tripId);
}
