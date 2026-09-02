package com.travelplanner.packing.service;

import com.travelplanner.auth.entity.User;
import com.travelplanner.auth.repository.UserRepository;
import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.packing.dto.PackingItemCreateRequest;
import com.travelplanner.packing.dto.PackingItemResponse;
import com.travelplanner.packing.dto.PackingItemUpdateRequest;
import com.travelplanner.packing.entity.PackingItem;
import com.travelplanner.packing.repository.PackingItemRepository;
import com.travelplanner.trip.entity.Trip;
import com.travelplanner.trip.entity.TripMember;
import com.travelplanner.trip.entity.TripRole;
import com.travelplanner.trip.repository.TripMemberRepository;
import com.travelplanner.trip.service.TripQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackingServiceTest {

    @Mock
    private PackingItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    private UserQueryService userQueryService;
    private TripQueryService tripQueryService;
    private PackingService packingService;

    private final String email = "user@example.com";
    private final Long userId = 10L;
    private final Long tripId = 1L;

    @BeforeEach
    void setUp() {
        userQueryService = new UserQueryService(userRepository);
        tripQueryService = new TripQueryService(tripMemberRepository);
        packingService = new PackingService(itemRepository, tripQueryService, userQueryService);
    }

    private void setupMemberUser() {
        User user = new User("user@example.com", "hash", "Test User");
        user.setId(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Trip trip = new Trip();
        trip.setId(tripId);
        TripMember member = new TripMember(trip, userId, TripRole.MEMBER);
        when(tripMemberRepository.findByTripId(tripId)).thenReturn(List.of(member));
    }

    private void setupNonMemberUser() {
        User user = new User("user@example.com", "hash", "Test User");
        user.setId(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripId(tripId)).thenReturn(Collections.emptyList());
    }

    @Test
    void createItem_Success() {
        setupMemberUser();

        PackingItem savedItem = new PackingItem(tripId, "Passport", 5L);
        savedItem.setId(100L);
        when(itemRepository.save(any(PackingItem.class))).thenReturn(savedItem);

        PackingItemCreateRequest request = new PackingItemCreateRequest("Passport", 5L);
        PackingItemResponse response = packingService.createItem(tripId, request, email);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.tripId()).isEqualTo(tripId);
        assertThat(response.label()).isEqualTo("Passport");
        assertThat(response.isChecked()).isFalse();
        assertThat(response.assignedUserId()).isEqualTo(5L);

        verify(itemRepository).save(any(PackingItem.class));
    }

    @Test
    void createItem_AccessDenied() {
        setupNonMemberUser();

        PackingItemCreateRequest request = new PackingItemCreateRequest("Sunscreen", null);

        assertThatThrownBy(() -> packingService.createItem(tripId, request, email))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not a member");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void getTripItems_Success() {
        setupMemberUser();

        PackingItem item1 = new PackingItem(tripId, "Item 1", null);
        item1.setId(1L);
        PackingItem item2 = new PackingItem(tripId, "Item 2", 20L);
        item2.setId(2L);

        when(itemRepository.findByTripIdOrderByIdAsc(tripId)).thenReturn(List.of(item1, item2));

        List<PackingItemResponse> result = packingService.getTripItems(tripId, email);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).label()).isEqualTo("Item 1");
        assertThat(result.get(1).assignedUserId()).isEqualTo(20L);
    }

    @Test
    void updateItem_ToggleCheckedAndAssignUser_Success() {
        PackingItem existing = new PackingItem(tripId, "Tent", null);
        existing.setId(50L);
        existing.setChecked(false);

        when(itemRepository.findById(50L)).thenReturn(Optional.of(existing));
        setupMemberUser();
        when(itemRepository.save(existing)).thenReturn(existing);

        PackingItemUpdateRequest request = new PackingItemUpdateRequest(true, 15L);
        PackingItemResponse response = packingService.updateItem(50L, request, email);

        assertThat(response.isChecked()).isTrue();
        assertThat(response.assignedUserId()).isEqualTo(15L);
    }

    @Test
    void updateItem_NotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        PackingItemUpdateRequest request = new PackingItemUpdateRequest(true, null);

        assertThatThrownBy(() -> packingService.updateItem(999L, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void deleteItem_Success() {
        PackingItem existing = new PackingItem(tripId, "Sleeping Bag", null);
        existing.setId(70L);

        when(itemRepository.findById(70L)).thenReturn(Optional.of(existing));
        setupMemberUser();

        packingService.deleteItem(70L, email);

        verify(itemRepository).delete(existing);
    }
}
