package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.entity.ItineraryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {
    
    // Acquires a row-level lock (FOR UPDATE) to prevent race conditions during batch updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ItineraryItem i WHERE i.day.id = :dayId")
    List<ItineraryItem> findByDayIdWithPessimisticWriteLock(@Param("dayId") Long dayId);
}

