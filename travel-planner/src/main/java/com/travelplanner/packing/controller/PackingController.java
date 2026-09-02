package com.travelplanner.packing.controller;

import com.travelplanner.packing.dto.PackingItemCreateRequest;
import com.travelplanner.packing.dto.PackingItemResponse;
import com.travelplanner.packing.dto.PackingItemUpdateRequest;
import com.travelplanner.packing.service.PackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PackingController {

    private final PackingService packingService;

    public PackingController(PackingService packingService) {
        this.packingService = packingService;
    }

    @PostMapping("/trips/{tripId}/packing-items")
    public ResponseEntity<PackingItemResponse> createItem(
            @PathVariable Long tripId,
            @RequestBody PackingItemCreateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(packingService.createItem(tripId, request, auth.getName()));
    }

    @GetMapping("/trips/{tripId}/packing-items")
    public ResponseEntity<List<PackingItemResponse>> getTripItems(
            @PathVariable Long tripId,
            Authentication auth) {
        return ResponseEntity.ok(packingService.getTripItems(tripId, auth.getName()));
    }

    @PatchMapping("/packing-items/{itemId}")
    public ResponseEntity<PackingItemResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody PackingItemUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(packingService.updateItem(itemId, request, auth.getName()));
    }

    @DeleteMapping("/packing-items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            Authentication auth) {
        packingService.deleteItem(itemId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
