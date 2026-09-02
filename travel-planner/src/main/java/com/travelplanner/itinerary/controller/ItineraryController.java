package com.travelplanner.itinerary.controller;

import com.travelplanner.itinerary.dto.DayCreateRequest;
import com.travelplanner.itinerary.dto.ItemCreateRequest;
import com.travelplanner.itinerary.dto.ItineraryResponse;
import com.travelplanner.itinerary.dto.ReorderRequest;
import com.travelplanner.itinerary.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/trips/{tripId}/days")
    public ResponseEntity<ItineraryResponse> createDay(@PathVariable Long tripId, @RequestBody DayCreateRequest request, Authentication auth) {
        return ResponseEntity.ok(itineraryService.createDay(tripId, request, auth.getName()));
    }

    @PostMapping("/itineraries/days/{dayId}/items")
    public ResponseEntity<ItineraryResponse> addItem(@PathVariable Long dayId, @RequestBody ItemCreateRequest request, Authentication auth) {
        return ResponseEntity.ok(itineraryService.addItemToDay(dayId, request, auth.getName()));
    }

    @PatchMapping("/itineraries/days/{dayId}/reorder")
    public ResponseEntity<ItineraryResponse> reorderItems(
            @PathVariable Long dayId, 
            @RequestBody ReorderRequest request, 
            Authentication auth) {
        
        return ResponseEntity.ok(itineraryService.reorderItems(dayId, request, auth.getName()));
    }

    @GetMapping("/trips/{tripId}/itinerary")
    public ResponseEntity<List<ItineraryResponse>> getItinerary(@PathVariable Long tripId, Authentication auth) {
        return ResponseEntity.ok(itineraryService.getTripItinerary(tripId, auth.getName()));
    }
}
