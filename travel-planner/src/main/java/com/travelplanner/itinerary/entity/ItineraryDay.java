package com.travelplanner.itinerary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itinerary_days")
public class ItineraryDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "day_date", nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC, startTime ASC")
    private List<ItineraryItem> items = new ArrayList<>();

    public ItineraryDay() {}
    public ItineraryDay(Long tripId, LocalDate date) {
        this.tripId = tripId;
        this.date = date;
    }

    public Long getId() { return id; }
    public Long getTripId() { return tripId; }
    public LocalDate getDate() { return date; }
    public List<ItineraryItem> getItems() { return items; }
    public void setItems(List<ItineraryItem> items) { this.items = items; }
}
