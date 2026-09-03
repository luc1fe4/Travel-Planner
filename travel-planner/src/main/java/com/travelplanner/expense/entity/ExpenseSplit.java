package com.travelplanner.expense.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits")
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_entry_id", nullable = false)
    private ExpenseEntry expenseEntry;

    @Column(name = "owed_by_user_id", nullable = false)
    private Long owedByUserId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    public ExpenseSplit() {}

    public ExpenseSplit(ExpenseEntry expenseEntry, Long owedByUserId, BigDecimal amount) {
        this.expenseEntry = expenseEntry;
        this.owedByUserId = owedByUserId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public ExpenseEntry getExpenseEntry() { return expenseEntry; }
    public void setExpenseEntry(ExpenseEntry expenseEntry) { this.expenseEntry = expenseEntry; }
    public Long getOwedByUserId() { return owedByUserId; }
    public void setOwedByUserId(Long owedByUserId) { this.owedByUserId = owedByUserId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
