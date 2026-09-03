package com.travelplanner.expense.dto;

import java.math.BigDecimal;

public record ExpenseSplitResponse(
    Long id,
    Long owedByUserId,
    BigDecimal amount
) {}
