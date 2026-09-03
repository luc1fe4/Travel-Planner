package com.travelplanner.expense.service;

import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.expense.dto.ExpenseCreateRequest;
import com.travelplanner.expense.dto.ExpenseResponse;
import com.travelplanner.expense.dto.ExpenseSplitResponse;
import com.travelplanner.expense.entity.ExpenseEntry;
import com.travelplanner.expense.entity.ExpenseSplit;
import com.travelplanner.expense.repository.ExpenseEntryRepository;
import com.travelplanner.trip.service.TripQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseEntryRepository expenseRepository;
    private final TripQueryService tripQueryService;
    private final UserQueryService userQueryService;

    public ExpenseService(ExpenseEntryRepository expenseRepository,
                          TripQueryService tripQueryService,
                          UserQueryService userQueryService) {
        this.expenseRepository = expenseRepository;
        this.tripQueryService = tripQueryService;
        this.userQueryService = userQueryService;
    }

    private void validateTripAccess(Long tripId, String email) {
        Long userId = userQueryService.getUserIdByEmail(email);
        if (!tripQueryService.isUserMemberOfTrip(tripId, userId)) {
            throw new SecurityException("User is not a member of this trip");
        }
    }

    @Transactional
    public ExpenseResponse recordExpense(Long tripId, ExpenseCreateRequest request, String email) {
        validateTripAccess(tripId, email);
        Long paidByUserId = userQueryService.getUserIdByEmail(email);

        if (request.splitUserIds() == null || request.splitUserIds().isEmpty()) {
            throw new IllegalArgumentException("Split participants list cannot be empty");
        }

        BigDecimal normalizedTotal = request.amount().setScale(2, RoundingMode.HALF_UP);
        ExpenseEntry entry = new ExpenseEntry(tripId, request.description(), normalizedTotal, paidByUserId);

        // Distribute cents evenly to eliminate rounding drift
        long totalCents = normalizedTotal.movePointRight(2).longValueExact();
        int count = request.splitUserIds().size();
        long baseCents = totalCents / count;
        long remainderCents = totalCents % count;

        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long splitUserId = request.splitUserIds().get(i);
            long centsForUser = baseCents + (i < remainderCents ? 1 : 0);
            BigDecimal splitAmount = BigDecimal.valueOf(centsForUser, 2);

            splits.add(new ExpenseSplit(entry, splitUserId, splitAmount));
        }

        entry.setSplits(splits);
        ExpenseEntry saved = expenseRepository.save(entry);
        return mapToResponse(saved);
    }

    public List<ExpenseResponse> getTripExpenses(Long tripId, String email) {
        validateTripAccess(tripId, email);
        return expenseRepository.findByTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ExpenseResponse mapToResponse(ExpenseEntry entry) {
        List<ExpenseSplitResponse> splitResponses = entry.getSplits().stream()
                .map(s -> new ExpenseSplitResponse(s.getId(), s.getOwedByUserId(), s.getAmount()))
                .toList();

        return new ExpenseResponse(
                entry.getId(),
                entry.getTripId(),
                entry.getDescription(),
                entry.getAmount(),
                entry.getPaidByUserId(),
                entry.getCreatedAt(),
                splitResponses
        );
    }
}
