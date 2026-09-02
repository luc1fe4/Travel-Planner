package com.travelplanner.trip.controller;

import com.travelplanner.trip.dto.JoinTripRequest;
import com.travelplanner.trip.dto.TripCreateRequest;
import com.travelplanner.trip.dto.TripResponse;
import com.travelplanner.trip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody TripCreateRequest request, Authentication authentication) {
        return ResponseEntity.ok(tripService.createTrip(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getUserTrips(Authentication authentication) {
        return ResponseEntity.ok(tripService.getUserTrips(authentication.getName()));
    }

    @PostMapping("/join")
    public ResponseEntity<TripResponse> joinTrip(@RequestBody JoinTripRequest request, Authentication authentication) {
        return ResponseEntity.ok(tripService.joinTrip(request.inviteCode(), authentication.getName()));
    }
}
