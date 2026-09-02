package com.travelplanner.trip.dto;

import java.time.LocalDate;

public record TripResponse(Long id, String name, LocalDate startDate, LocalDate endDate, String inviteCode) {}
