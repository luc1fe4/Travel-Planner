package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {}
