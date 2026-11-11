package org.delcom.ifs25041springbootcashflow.usecase;

import org.delcom.ifs25041springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25041springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25041springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER = 1;
    private static final int OTHER = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void addIncomeAndExpenseTrimInput() throws Exception {
        Transaction income = useCase.addIncome(USER, "  Gaji  ", " 5000000 ");
        Transaction expense = useCase.addExpense(USER, "Makan", "25000");

        assertEquals("Gaji", income.getDescription());
        assertEquals(5_000_000L, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertNull(income.getProofPath());
        assertEquals(2, useCase.getTransactionsByUser(USER).size());
    }

    @Test
    void addTransactionWithProof() throws Exception {
        Transaction transaction = useCase.addTransaction(USER, "Struk", "100", TransactionType.EXPENSE, "proofs/a.png");
        assertEquals("proofs/a.png", transaction.getProofPath());
    }

    @Test
    void addRejectsInvalidInput() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, null, "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "   ", "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", "  "));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", "12.5"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", "99999999999999999999"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "A", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "-5"));
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());
    }

    @Test
    void getAllTransactionsSpansUsers() throws Exception {
        useCase.addIncome(USER, "A", "1");
        useCase.addIncome(OTHER, "B", "2");
        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(1, useCase.getTransactionsByUser(OTHER).size());
    }

    @Test
    void removeOwnTransaction() throws Exception {
        Transaction transaction = useCase.addIncome(USER, "A", "1");
        useCase.removeTransactionForUser(USER, transaction.getId());
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());
    }

    @Test
    void removeValidatesIdOwnershipAndExistence() throws Exception {
        Transaction transaction = useCase.addIncome(USER, "A", "1");

        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(USER, 0));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(USER, -3));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER, 999));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(OTHER, transaction.getId()));
        assertEquals(1, useCase.getTransactionsByUser(USER).size());
    }

    @Test
    void removeFailsWhenRepositoryCannotDelete() throws Exception {
        TransactionRepository stale = new TransactionRepository() {
            @Override
            public boolean deleteById(Integer id) {
                return false;
            }
        };
        FinanceUseCase staleUseCase = new FinanceUseCase(stale);
        Transaction transaction = staleUseCase.addIncome(USER, "A", "1");

        assertThrows(EntityNotFoundException.class,
                () -> staleUseCase.removeTransactionForUser(USER, transaction.getId()));
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction transaction = useCase.addIncome(USER, "Gaji", "100");

        useCase.updateTransactionForUser(USER, transaction.getId(), null, null, null, null, false);
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(100L, transaction.getAmount());

        useCase.updateTransactionForUser(USER, transaction.getId(), " Bonus ", "250", TransactionType.EXPENSE,
                "proofs/a.png", false);
        assertEquals("Bonus", transaction.getDescription());
        assertEquals(250L, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("proofs/a.png", transaction.getProofPath());

        useCase.updateTransactionForUser(USER, transaction.getId(), null, null, null, "proofs/ignored.png", true);
        assertNull(transaction.getProofPath());
    }

    @Test
    void updateRejectsInvalidInputAndForeignOwner() throws Exception {
        Transaction transaction = useCase.addIncome(USER, "Gaji", "100");

        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, transaction.getId(), " ", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, transaction.getId(), null, "x", null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(OTHER, transaction.getId(), "A", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, 0, "A", null, null, null, false));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(USER, 999, "A", null, null, null, false));
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws Exception {
        useCase.addIncome(USER, "Gaji Bulanan", "100");
        useCase.addExpense(USER, "Makan siang", "10");
        useCase.addExpense(OTHER, "Gaji orang lain", "10");

        assertEquals(1, useCase.searchTransactions(USER, "GAJI").size());
        assertEquals(1, useCase.searchTransactions(USER, "  siang ").size());
        assertEquals(2, useCase.searchTransactions(USER, "").size());
        assertEquals(2, useCase.searchTransactions(USER, null).size());
        assertTrue(useCase.searchTransactions(USER, "tidak ada").isEmpty());
    }

    @Test
    void sortUsesSortOption() throws Exception {
        useCase.addExpense(USER, "Makan", "25000");
        useCase.addIncome(USER, "Gaji", "5000000");
        useCase.addExpense(USER, "Parkir", "5000");

        List<Transaction> ascending = useCase.sortTransactions(USER, SortOption.AMOUNT_ASC);
        assertEquals(5_000L, ascending.get(0).getAmount());

        List<Transaction> incomeFirst = useCase.sortTransactions(USER, SortOption.INCOME_FIRST);
        assertEquals(TransactionType.INCOME, incomeFirst.get(0).getType());
    }

    @Test
    void balanceAddsIncomeAndSubtractsExpense() throws Exception {
        useCase.addIncome(USER, "Gaji", "5000000");
        useCase.addExpense(USER, "Makan", "25000");
        useCase.addIncome(OTHER, "Bukan saya", "999");

        BalanceSummary summary = useCase.getBalance(USER);
        assertEquals(2, summary.count());
        assertEquals(5_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());
        assertEquals(4_975_000L, summary.balance());

        useCase.removeTransactionForUser(USER, 1);
        assertEquals(-25_000L, useCase.getBalance(USER).balance());
        assertEquals(0L, useCase.getBalance(99).balance());
    }
}
