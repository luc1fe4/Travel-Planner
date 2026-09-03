package com.travelplanner.expense.repository;

import com.travelplanner.expense.entity.ExpenseEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findByTripIdOrderByCreatedAtDesc(Long tripId);
}
