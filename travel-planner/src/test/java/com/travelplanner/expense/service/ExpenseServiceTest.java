package com.travelplanner.expense.service;

import com.travelplanner.auth.entity.User;
import com.travelplanner.auth.repository.UserRepository;
import com.travelplanner.auth.service.UserQueryService;
import com.travelplanner.expense.dto.ExpenseCreateRequest;
import com.travelplanner.expense.dto.ExpenseResponse;
import com.travelplanner.expense.dto.ExpenseSplitResponse;
import com.travelplanner.expense.dto.TripBalanceResponse;
import com.travelplanner.expense.entity.ExpenseEntry;
import com.travelplanner.expense.entity.ExpenseSplit;
import com.travelplanner.expense.repository.ExpenseEntryRepository;
import com.travelplanner.trip.entity.Trip;
import com.travelplanner.trip.entity.TripMember;
import com.travelplanner.trip.entity.TripRole;
import com.travelplanner.trip.repository.TripMemberRepository;
import com.travelplanner.trip.service.TripQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseEntryRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    private UserQueryService userQueryService;
    private TripQueryService tripQueryService;
    private ExpenseService expenseService;

    private final String email = "payer@example.com";
    private final Long userId = 1L;
    private final Long tripId = 10L;

    @BeforeEach
    void setUp() {
        userQueryService = new UserQueryService(userRepository);
        tripQueryService = new TripQueryService(tripMemberRepository);
        expenseService = new ExpenseService(expenseRepository, tripQueryService, userQueryService);
    }

    private void setupMemberUser() {
        User user = new User(email, "hash", "Payer User");
        user.setId(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Trip trip = new Trip();
        trip.setId(tripId);
        TripMember member = new TripMember(trip, userId, TripRole.MEMBER);
        when(tripMemberRepository.findByTripId(tripId)).thenReturn(List.of(member));
    }

    private void setupNonMemberUser() {
        User user = new User(email, "hash", "Non Member");
        user.setId(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripId(tripId)).thenReturn(Collections.emptyList());
    }

    @Test
    void recordExpense_Success_EvenSplit() {
        setupMemberUser();

        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Dinner",
                new BigDecimal("100.00"),
                List.of(1L, 2L)
        );

        when(expenseRepository.save(any(ExpenseEntry.class))).thenAnswer(invocation -> {
            ExpenseEntry entry = invocation.getArgument(0);
            return entry;
        });

        ExpenseResponse response = expenseService.recordExpense(tripId, request, email);

        assertThat(response).isNotNull();
        assertThat(response.description()).isEqualTo("Dinner");
        assertThat(response.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.paidByUserId()).isEqualTo(userId);
        assertThat(response.splits()).hasSize(2);
        assertThat(response.splits().get(0).amount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(response.splits().get(1).amount()).isEqualTo(new BigDecimal("50.00"));

        verify(expenseRepository).save(any(ExpenseEntry.class));
    }

    @Test
    void recordExpense_Success_UnevenRemainderSplit() {
        setupMemberUser();

        // $100.00 / 3 participants -> 10000 cents / 3 = 3333 cents with 1 cent remainder
        // User 1 gets 33.34, User 2 gets 33.33, User 3 gets 33.33
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Taxi",
                new BigDecimal("100.00"),
                List.of(1L, 2L, 3L)
        );

        when(expenseRepository.save(any(ExpenseEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response = expenseService.recordExpense(tripId, request, email);

        assertThat(response.splits()).hasSize(3);
        assertThat(response.splits().get(0).amount()).isEqualTo(new BigDecimal("33.34"));
        assertThat(response.splits().get(1).amount()).isEqualTo(new BigDecimal("33.33"));
        assertThat(response.splits().get(2).amount()).isEqualTo(new BigDecimal("33.33"));

        BigDecimal sumOfSplits = response.splits().stream()
                .map(ExpenseSplitResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sumOfSplits).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void recordExpense_EmptySplitUsers_ThrowsException() {
        setupMemberUser();

        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Snacks",
                new BigDecimal("15.00"),
                Collections.emptyList()
        );

        assertThatThrownBy(() -> expenseService.recordExpense(tripId, request, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Split participants list cannot be empty");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void recordExpense_NonMember_ThrowsSecurityException() {
        setupNonMemberUser();

        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Hotel",
                new BigDecimal("200.00"),
                List.of(1L)
        );

        assertThatThrownBy(() -> expenseService.recordExpense(tripId, request, email))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("User is not a member of this trip");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void getTripExpenses_Success() {
        setupMemberUser();

        ExpenseEntry entry1 = new ExpenseEntry(tripId, "Museum", new BigDecimal("30.00"), userId);
        ExpenseEntry entry2 = new ExpenseEntry(tripId, "Lunch", new BigDecimal("45.00"), userId);

        when(expenseRepository.findByTripIdOrderByCreatedAtDesc(tripId)).thenReturn(List.of(entry1, entry2));

        List<ExpenseResponse> results = expenseService.getTripExpenses(tripId, email);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).description()).isEqualTo("Museum");
        assertThat(results.get(1).description()).isEqualTo("Lunch");
    }

    @Test
    void computeBalances_Success() {
        setupMemberUser();

        // User 1 pays $60 for User 1 and User 2 ($30 each)
        ExpenseEntry entry = new ExpenseEntry(tripId, "Dinner", new BigDecimal("60.00"), 1L);
        ExpenseSplit split1 = new ExpenseSplit(entry, 1L, new BigDecimal("30.00"));
        ExpenseSplit split2 = new ExpenseSplit(entry, 2L, new BigDecimal("30.00"));
        entry.setSplits(List.of(split1, split2));

        when(expenseRepository.findByTripIdWithSplits(tripId)).thenReturn(List.of(entry));

        TripBalanceResponse balance = expenseService.computeBalances(tripId, email);

        assertThat(balance).isNotNull();
        assertThat(balance.netBalances()).hasSize(2);

        // User 1 net: +$30.00 (paid $60, owes $30)
        // User 2 net: -$30.00 (paid $0, owes $30)
        TripBalanceResponse.UserBalance ub1 = balance.netBalances().stream()
                .filter(b -> b.userId().equals(1L))
                .findFirst().orElseThrow();
        assertThat(ub1.netAmount()).isEqualTo(new BigDecimal("30.00"));

        TripBalanceResponse.UserBalance ub2 = balance.netBalances().stream()
                .filter(b -> b.userId().equals(2L))
                .findFirst().orElseThrow();
        assertThat(ub2.netAmount()).isEqualTo(new BigDecimal("-30.00"));

        // Pairwise settlement: User 2 owes User 1 $30.00
        assertThat(balance.settlements()).hasSize(1);
        TripBalanceResponse.PairwiseSettlement settlement = balance.settlements().get(0);
        assertThat(settlement.fromUserId()).isEqualTo(2L);
        assertThat(settlement.toUserId()).isEqualTo(1L);
        assertThat(settlement.amount()).isEqualTo(new BigDecimal("30.00"));
    }
}
