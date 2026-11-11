package org.delcom.ifs25041springbootcashflow.adapter.repository;

import org.delcom.ifs25041springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveAssignsIncrementalIds() {
        Transaction first = repository.save(1, "Gaji", 1000L, TransactionType.INCOME, null);
        Transaction second = repository.save(1, "Makan", 50L, TransactionType.EXPENSE, "proofs/a.png");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals("proofs/a.png", second.getProofPath());
        assertEquals(2, repository.findAll().size());
        assertEquals(2, repository.findByUserId(1).size());
        assertTrue(repository.findByUserId(2).isEmpty());
    }

    @Test
    void findByIdAndDelete() {
        Transaction transaction = repository.save(1, "Hapus saya", 10L, TransactionType.EXPENSE, null);

        Optional<Transaction> found = repository.findById(transaction.getId());
        assertTrue(found.isPresent());
        assertEquals("Hapus saya", found.get().getDescription());

        assertTrue(repository.deleteById(transaction.getId()));
        assertFalse(repository.deleteById(transaction.getId()));
        assertTrue(repository.findById(transaction.getId()).isEmpty());
    }

    @Test
    void findByPredicateAndUpdateNoOp() {
        repository.save(1, "Belanja Java", 10L, TransactionType.EXPENSE, null);
        repository.save(1, "Belanja Spring", 20L, TransactionType.EXPENSE, null);

        List<Transaction> spring = repository.findBy(t -> t.getDescription().contains("Spring"));
        assertEquals(1, spring.size());

        Transaction transaction = spring.get(0);
        transaction.changeDescription("Belanja Spring Boot");
        repository.update(transaction);
        assertEquals("Belanja Spring Boot", repository.findById(transaction.getId()).orElseThrow().getDescription());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.save(1, "A", 1L, TransactionType.INCOME, null);
        List<Transaction> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveEntityDirectly() {
        repository.save(new Transaction(99, 1, "Manual", 5L, TransactionType.INCOME));
        assertTrue(repository.findById(99).isPresent());
    }
}
