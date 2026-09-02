package com.travelplanner.trip.dto;

import java.time.LocalDate;

public record TripCreateRequest(String name, LocalDate startDate, LocalDate endDate) {}
