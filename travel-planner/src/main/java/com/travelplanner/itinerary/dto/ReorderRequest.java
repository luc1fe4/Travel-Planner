package com.travelplanner.itinerary.dto;

import java.util.List;

public record ReorderRequest(List<ItemOrder> items) {
    public record ItemOrder(Long itemId, Integer newIndex) {}
}
