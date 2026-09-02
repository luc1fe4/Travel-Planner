package com.travelplanner.trip.repository;

import com.travelplanner.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
  List<TripMember> findByUserId(Long userId);

  List<TripMember> findByTripId(Long tripId);
}