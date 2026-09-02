package com.travelplanner.itinerary.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "itinerary_items")
public class ItineraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private ItineraryDay day;

    @Column(nullable = false)
    private String title;

    @Column(name = "location_name")
    private String locationName;

    private Double lat;
    private Double lng;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "start_time")
    private LocalTime startTime;

    public ItineraryItem() {}
    public ItineraryItem(ItineraryDay day, String title, String locationName, Double lat, Double lng, Integer sequenceOrder, LocalTime startTime) {
        this.day = day;
        this.title = title;
        this.locationName = locationName;
        this.lat = lat;
        this.lng = lng;
        this.sequenceOrder = sequenceOrder;
        this.startTime = startTime;
    }

    // Standard getters
    public Long getId() { return id; }
    public ItineraryDay getDay() { return day; }
    public String getTitle() { return title; }
    public String getLocationName() { return locationName; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public LocalTime getStartTime() { return startTime; }
}
