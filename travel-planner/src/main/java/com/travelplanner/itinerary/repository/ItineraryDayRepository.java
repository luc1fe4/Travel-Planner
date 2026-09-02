package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.entity.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {
    List<ItineraryDay> findByTripIdOrderByDateAsc(Long tripId);
}
