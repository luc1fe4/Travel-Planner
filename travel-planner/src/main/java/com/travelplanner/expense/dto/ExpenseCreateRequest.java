package com.travelplanner.expense.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseCreateRequest(
    String description,
    BigDecimal amount,
    List<Long> splitUserIds
) {}
