package com.travelplanner.packing.service;

import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.packing.dto.PackingItemCreateRequest;
import com.travelplanner.packing.dto.PackingItemResponse;
import com.travelplanner.packing.dto.PackingItemUpdateRequest;
import com.travelplanner.packing.entity.PackingItem;
import com.travelplanner.packing.repository.PackingItemRepository;
import com.travelplanner.trip.service.TripQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackingService {

    private final PackingItemRepository itemRepository;
    private final TripQueryService tripQueryService;
    private final UserQueryService userQueryService;

    public PackingService(PackingItemRepository itemRepository,
                          TripQueryService tripQueryService,
                          UserQueryService userQueryService) {
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
    public PackingItemResponse createItem(Long tripId, PackingItemCreateRequest request, String email) {
        validateTripAccess(tripId, email);
        PackingItem item = new PackingItem(tripId, request.label(), request.assignedUserId());
        return mapToResponse(itemRepository.save(item));
    }

    public List<PackingItemResponse> getTripItems(Long tripId, String email) {
        validateTripAccess(tripId, email);
        return itemRepository.findByTripIdOrderByIdAsc(tripId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PackingItemResponse updateItem(Long itemId, PackingItemUpdateRequest request, String email) {
        PackingItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        validateTripAccess(item.getTripId(), email);

        if (request.isChecked() != null) {
            item.setChecked(request.isChecked());
        }
        if (request.assignedUserId() != null) {
            item.setAssignedUserId(request.assignedUserId());
        }

        return mapToResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId, String email) {
        PackingItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        validateTripAccess(item.getTripId(), email);
        itemRepository.delete(item);
    }

    private PackingItemResponse mapToResponse(PackingItem item) {
        return new PackingItemResponse(
                item.getId(),
                item.getTripId(),
                item.getLabel(),
                item.isChecked(),
                item.getAssignedUserId()
        );
    }
}
