package com.travelplanner.trip.service;

import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.trip.dto.TripCreateRequest;
import com.travelplanner.trip.dto.TripResponse;
import com.travelplanner.trip.entity.Trip;
import com.travelplanner.trip.entity.TripMember;
import com.travelplanner.trip.entity.TripRole;
import com.travelplanner.trip.repository.TripMemberRepository;
import com.travelplanner.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserQueryService userQueryService;

    public TripService(TripRepository tripRepository, TripMemberRepository tripMemberRepository, UserQueryService userQueryService) {
        this.tripRepository = tripRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.userQueryService = userQueryService;
    }

    @Transactional
    public TripResponse createTrip(TripCreateRequest request, String userEmail) {
        Long userId = userQueryService.getUserIdByEmail(userEmail);
        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        Trip trip = new Trip(request.name(), request.startDate(), request.endDate(), userId);
        trip.setInviteCode(inviteCode);
        Trip savedTrip = tripRepository.save(trip);

        TripMember owner = new TripMember(savedTrip, userId, TripRole.OWNER);
        tripMemberRepository.save(owner);

        return new TripResponse(savedTrip.getId(), savedTrip.getName(), savedTrip.getStartDate(), savedTrip.getEndDate(), savedTrip.getInviteCode());
    }

    public List<TripResponse> getUserTrips(String userEmail) {
        Long userId = userQueryService.getUserIdByEmail(userEmail);
        return tripMemberRepository.findByUserId(userId).stream()
                .map(TripMember::getTrip)
                .map(trip -> new TripResponse(trip.getId(), trip.getName(), trip.getStartDate(), trip.getEndDate(), trip.getInviteCode()))
                .toList();
    }

    @Transactional
    public TripResponse joinTrip(String inviteCode, String userEmail) {
        Long userId = userQueryService.getUserIdByEmail(userEmail);
        Trip trip = tripRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        boolean alreadyMember = tripMemberRepository.findByTripId(trip.getId()).stream()
                .anyMatch(member -> member.getUserId().equals(userId));

        if (!alreadyMember) {
            TripMember newMember = new TripMember(trip, userId, TripRole.MEMBER);
            tripMemberRepository.save(newMember);
        }

        return new TripResponse(trip.getId(), trip.getName(), trip.getStartDate(), trip.getEndDate(), trip.getInviteCode());
    }
}
