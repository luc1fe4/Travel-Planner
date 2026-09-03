package com.travelplanner.expense.dto;

import java.math.BigDecimal;
import java.util.List;

public record TripBalanceResponse(
    List<UserBalance> netBalances,
    List<PairwiseSettlement> settlements
) {
    public record UserBalance(Long userId, BigDecimal netAmount) {}
    public record PairwiseSettlement(Long fromUserId, Long toUserId, BigDecimal amount) {}
}
