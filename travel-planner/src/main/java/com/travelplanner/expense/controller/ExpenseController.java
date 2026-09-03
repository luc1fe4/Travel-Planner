package com.travelplanner.expense.controller;

import com.travelplanner.expense.dto.ExpenseCreateRequest;
import com.travelplanner.expense.dto.ExpenseResponse;
import com.travelplanner.expense.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> recordExpense(
            @PathVariable Long tripId,
            @RequestBody ExpenseCreateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(expenseService.recordExpense(tripId, request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getTripExpenses(
            @PathVariable Long tripId,
            Authentication auth) {
        return ResponseEntity.ok(expenseService.getTripExpenses(tripId, auth.getName()));
    }
}
