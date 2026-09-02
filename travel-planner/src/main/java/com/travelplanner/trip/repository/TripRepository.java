package com.travelplanner.trip.repository;

import com.travelplanner.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByOwnerId(Long ownerId);
    Optional<Trip> findByInviteCode(String inviteCode);
}
