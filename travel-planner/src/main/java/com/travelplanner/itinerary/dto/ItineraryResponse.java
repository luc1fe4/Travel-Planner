package com.travelplanner.itinerary.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ItineraryResponse(Long id, LocalDate date, List<ItemResponse> items) {
    public record ItemResponse(Long id, String title, String locationName, Double lat, Double lng, Integer sequenceOrder, LocalTime startTime) {}
}
