package org.delcom.ifs25041springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void shortConstructorSetsDefaults() {
        Transaction transaction = new Transaction(1, 7, "Gaji", 5_000_000L, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals(7, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNull(transaction.getProofPath());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void fullConstructorKeepsProofAndTimestamp() {
        LocalDateTime at = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction transaction = new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE, "proofs/a.png", at);

        assertEquals("proofs/a.png", transaction.getProofPath());
        assertEquals(at, transaction.getCreatedAt());
    }

    @Test
    void incomeAndExpenseSignedAmount() {
        Transaction income = new Transaction(1, 1, "Gaji", 100L, TransactionType.INCOME);
        Transaction expense = new Transaction(2, 1, "Makan", 40L, TransactionType.EXPENSE);

        assertTrue(income.isIncome());
        assertFalse(expense.isIncome());
        assertEquals(100L, income.signedAmount());
        assertEquals(-40L, expense.signedAmount());
    }

    @Test
    void mutatorsChangeFields() {
        Transaction transaction = new Transaction(1, 1, "A", 10L, TransactionType.INCOME);

        transaction.changeDescription("B");
        transaction.changeAmount(20L);
        transaction.changeType(TransactionType.EXPENSE);
        transaction.changeProofPath("proofs/x.png");

        assertEquals("B", transaction.getDescription());
        assertEquals(20L, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("proofs/x.png", transaction.getProofPath());
    }

    @Test
    void transactionTypeSign() {
        assertEquals(1, TransactionType.INCOME.sign());
        assertEquals(-1, TransactionType.EXPENSE.sign());
    }

    @Test
    void balanceSummaryComputesBalance() {
        BalanceSummary summary = new BalanceSummary(3, 5_000_000L, 25_000L);

        assertEquals(3, summary.count());
        assertEquals(5_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());
        assertEquals(4_975_000L, summary.balance());
    }
}
