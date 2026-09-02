package com.travelplanner.itinerary.dto;

import java.time.LocalTime;

public record ItemCreateRequest(String title, String locationName, Double lat, Double lng, Integer sequenceOrder, LocalTime startTime) {}
