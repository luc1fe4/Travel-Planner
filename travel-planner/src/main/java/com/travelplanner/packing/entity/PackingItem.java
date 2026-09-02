package com.travelplanner.packing.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "packing_items")
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private String label;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    public PackingItem() {}

    public PackingItem(Long tripId, String label, Long assignedUserId) {
        this.tripId = tripId;
        this.label = label;
        this.assignedUserId = assignedUserId;
        this.isChecked = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
}
