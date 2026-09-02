package com.travelplanner.packing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.packing.dto.PackingItemCreateRequest;
import com.travelplanner.packing.dto.PackingItemResponse;
import com.travelplanner.packing.dto.PackingItemUpdateRequest;
import com.travelplanner.packing.service.PackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PackingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestPackingService packingService;

    static class TestPackingService extends PackingService {
        PackingItemCreateRequest lastCreateRequest;
        Long lastCreateTripId;
        String lastCreateEmail;

        PackingItemUpdateRequest lastUpdateRequest;
        Long lastUpdateItemId;
        String lastUpdateEmail;

        Long lastDeleteItemId;
        String lastDeleteEmail;

        Long lastGetTripId;
        String lastGetEmail;

        public TestPackingService() {
            super(null, null, null);
        }

        @Override
        public PackingItemResponse createItem(Long tripId, PackingItemCreateRequest request, String email) {
            this.lastCreateTripId = tripId;
            this.lastCreateRequest = request;
            this.lastCreateEmail = email;
            return new PackingItemResponse(1L, tripId, request.label(), false, request.assignedUserId());
        }

        @Override
        public List<PackingItemResponse> getTripItems(Long tripId, String email) {
            this.lastGetTripId = tripId;
            this.lastGetEmail = email;
            return List.of(
                    new PackingItemResponse(1L, tripId, "Item 1", false, null),
                    new PackingItemResponse(2L, tripId, "Item 2", true, 5L)
            );
        }

        @Override
        public PackingItemResponse updateItem(Long itemId, PackingItemUpdateRequest request, String email) {
            this.lastUpdateItemId = itemId;
            this.lastUpdateRequest = request;
            this.lastUpdateEmail = email;
            return new PackingItemResponse(itemId, 10L, "Item 1", Boolean.TRUE.equals(request.isChecked()), request.assignedUserId());
        }

        @Override
        public void deleteItem(Long itemId, String email) {
            this.lastDeleteItemId = itemId;
            this.lastDeleteEmail = email;
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        packingService = new TestPackingService();
        PackingController controller = new PackingController(packingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Authentication createAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, "password", Collections.emptyList());
    }

    @Test
    void createItem_Returns200() throws Exception {
        PackingItemCreateRequest request = new PackingItemCreateRequest("Toothbrush", null);
        Authentication auth = createAuth("testuser@example.com");

        mockMvc.perform(post("/api/v1/trips/10/packing-items")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(10L))
                .andExpect(jsonPath("$.label").value("Toothbrush"))
                .andExpect(jsonPath("$.isChecked").value(false))
                .andExpect(jsonPath("$.assignedUserId").isEmpty());
    }

    @Test
    void getTripItems_Returns200() throws Exception {
        Authentication auth = createAuth("testuser@example.com");

        mockMvc.perform(get("/api/v1/trips/10/packing-items")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("Item 1"))
                .andExpect(jsonPath("$[1].assignedUserId").value(5L));
    }

    @Test
    void updateItem_Returns200() throws Exception {
        PackingItemUpdateRequest request = new PackingItemUpdateRequest(true, 5L);
        Authentication auth = createAuth("testuser@example.com");

        mockMvc.perform(patch("/api/v1/packing-items/1")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isChecked").value(true))
                .andExpect(jsonPath("$.assignedUserId").value(5L));
    }

    @Test
    void deleteItem_Returns204() throws Exception {
        Authentication auth = createAuth("testuser@example.com");

        mockMvc.perform(delete("/api/v1/packing-items/1")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }
}
