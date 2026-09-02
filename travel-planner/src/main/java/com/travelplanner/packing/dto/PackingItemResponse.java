package com.travelplanner.packing.dto;

public record PackingItemResponse(
    Long id,
    Long tripId,
    String label,
    boolean isChecked,
    Long assignedUserId
) {}
