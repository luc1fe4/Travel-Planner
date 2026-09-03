package com.travelplanner.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExpenseResponse(
    Long id,
    Long tripId,
    String description,
    BigDecimal amount,
    Long paidByUserId,
    Instant createdAt,
    List<ExpenseSplitResponse> splits
) {}
