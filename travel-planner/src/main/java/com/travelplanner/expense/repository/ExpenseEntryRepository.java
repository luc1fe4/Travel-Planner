package com.travelplanner.expense.repository;

import com.travelplanner.expense.entity.ExpenseEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findByTripIdOrderByCreatedAtDesc(Long tripId);

    @Query("SELECT DISTINCT e FROM ExpenseEntry e LEFT JOIN FETCH e.splits WHERE e.tripId = :tripId")
    List<ExpenseEntry> findByTripIdWithSplits(@Param("tripId") Long tripId);
}
