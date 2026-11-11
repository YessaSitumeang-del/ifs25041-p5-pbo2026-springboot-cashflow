package org.delcom.ifs25041springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    private static List<Transaction> sample() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction(1, 1, "Makan", 25_000L, TransactionType.EXPENSE));
        list.add(new Transaction(2, 1, "Gaji", 5_000_000L, TransactionType.INCOME));
        list.add(new Transaction(3, 1, "Parkir", 5_000L, TransactionType.EXPENSE));
        return list;
    }

    @Test
    void hasAtLeastFourOptions() {
        assertTrue(SortOption.values().length >= 4);
    }

    @Test
    void sortsByAmountAscendingAndDescending() {
        List<Transaction> list = sample();

        list.sort(SortOption.AMOUNT_ASC.comparator());
        assertEquals(5_000L, list.get(0).getAmount());
        assertEquals(5_000_000L, list.get(2).getAmount());

        list.sort(SortOption.AMOUNT_DESC.comparator());
        assertEquals(5_000_000L, list.get(0).getAmount());
        assertEquals(5_000L, list.get(2).getAmount());
    }

    @Test
    void sortsByType() {
        List<Transaction> list = sample();

        list.sort(SortOption.INCOME_FIRST.comparator());
        assertEquals(TransactionType.INCOME, list.get(0).getType());

        list.sort(SortOption.EXPENSE_FIRST.comparator());
        assertEquals(TransactionType.EXPENSE, list.get(0).getType());
        assertEquals(TransactionType.INCOME, list.get(2).getType());
    }
}
