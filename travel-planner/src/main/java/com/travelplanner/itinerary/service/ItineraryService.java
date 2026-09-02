package com.travelplanner.itinerary.service;

import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.itinerary.dto.DayCreateRequest;
import com.travelplanner.itinerary.dto.ItemCreateRequest;
import com.travelplanner.itinerary.dto.ItineraryResponse;
import com.travelplanner.itinerary.dto.ReorderRequest;
import com.travelplanner.itinerary.entity.ItineraryDay;
import com.travelplanner.itinerary.entity.ItineraryItem;
import com.travelplanner.itinerary.repository.ItineraryDayRepository;
import com.travelplanner.itinerary.repository.ItineraryItemRepository;
import com.travelplanner.trip.service.TripQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ItineraryService {
    private final ItineraryDayRepository dayRepository;
    private final ItineraryItemRepository itemRepository;
    private final TripQueryService tripQueryService;
    private final UserQueryService userQueryService;

    public ItineraryService(ItineraryDayRepository dayRepository, ItineraryItemRepository itemRepository, TripQueryService tripQueryService, UserQueryService userQueryService) {
        this.dayRepository = dayRepository;
        this.itemRepository = itemRepository;
        this.tripQueryService = tripQueryService;
        this.userQueryService = userQueryService;
    }

    private void validateTripAccess(Long tripId, String email) {
        Long userId = userQueryService.getUserIdByEmail(email);
        if (!tripQueryService.isUserMemberOfTrip(tripId, userId)) {
            throw new SecurityException("User is not a member of this trip");
        }
    }

    @Transactional
    public ItineraryResponse createDay(Long tripId, DayCreateRequest request, String email) {
        validateTripAccess(tripId, email);
        ItineraryDay day = dayRepository.save(new ItineraryDay(tripId, request.date()));
        return mapToResponse(day);
    }

    @Transactional
    public ItineraryResponse addItemToDay(Long dayId, ItemCreateRequest request, String email) {
        ItineraryDay day = dayRepository.findById(dayId).orElseThrow();
        validateTripAccess(day.getTripId(), email);
        
        ItineraryItem item = new ItineraryItem(day, request.title(), request.locationName(), request.lat(), request.lng(), request.sequenceOrder(), request.startTime());
        itemRepository.save(item);
        
        return mapToResponse(day);
    }

    @Transactional
    public ItineraryResponse reorderItems(Long dayId, ReorderRequest request, String email) {
        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));
        
        validateTripAccess(day.getTripId(), email);

        // Fetch items and lock them in the database for this transaction
        List<ItineraryItem> lockedItems = itemRepository.findByDayIdWithPessimisticWriteLock(dayId);

        // Apply the new sequence indexes in memory
        for (ReorderRequest.ItemOrder order : request.items()) {
            lockedItems.stream()
                    .filter(item -> item.getId().equals(order.itemId()))
                    .findFirst()
                    .ifPresent(item -> item.setSequenceOrder(order.newIndex()));
        }

        // Save the batch update
        itemRepository.saveAll(lockedItems);
        itemRepository.flush();

        // Resort the locked items in memory to guarantee the response matches the new state
        lockedItems.sort(java.util.Comparator.comparing(ItineraryItem::getSequenceOrder)
                .thenComparing(ItineraryItem::getStartTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        
        day.setItems(lockedItems);

        return mapToResponse(day);
    }

    public List<ItineraryResponse> getTripItinerary(Long tripId, String email) {
        validateTripAccess(tripId, email);
        return dayRepository.findByTripIdOrderByDateAsc(tripId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ItineraryResponse mapToResponse(ItineraryDay day) {
        List<ItineraryResponse.ItemResponse> itemResponses = day.getItems().stream()
                .map(item -> new ItineraryResponse.ItemResponse(item.getId(), item.getTitle(), item.getLocationName(), item.getLat(), item.getLng(), item.getSequenceOrder(), item.getStartTime()))
                .toList();
        return new ItineraryResponse(day.getId(), day.getDate(), itemResponses);
    }
}
