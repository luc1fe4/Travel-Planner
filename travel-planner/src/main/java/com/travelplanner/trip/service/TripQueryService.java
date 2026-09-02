package com.travelplanner.trip.service;

import com.travelplanner.trip.repository.TripMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class TripQueryService {
    private final TripMemberRepository tripMemberRepository;

    public TripQueryService(TripMemberRepository tripMemberRepository) {
        this.tripMemberRepository = tripMemberRepository;
    }

    public boolean isUserMemberOfTrip(Long tripId, Long userId) {
        return tripMemberRepository.findByTripId(tripId).stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }
}
