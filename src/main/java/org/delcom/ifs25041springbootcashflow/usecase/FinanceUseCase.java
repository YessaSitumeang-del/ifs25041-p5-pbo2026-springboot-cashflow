package org.delcom.ifs25041springbootcashflow.usecase;

import org.delcom.ifs25041springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25041springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25041springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25041springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25041springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 * Seluruh operasi bersifat scoped per pengguna.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi dari seluruh pengguna. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah pemasukan tanpa bukti. */
    public Transaction addIncome(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, description, amountText, TransactionType.INCOME, null);
    }

    /** Menambah pengeluaran tanpa bukti. */
    public Transaction addExpense(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, description, amountText, TransactionType.EXPENSE, null);
    }

    /** Menambah transaksi baru setelah validasi keterangan dan jumlah. */
    public Transaction addTransaction(
            int userId,
            String description,
            String amountText,
            TransactionType type,
            String proofPath
    ) throws ValidationException {
        String safeDescription = requireDescription(description);
        long amount = parseAmount(amountText);
        return transactionRepository.save(userId, safeDescription, amount, type, proofPath);
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        if (!transactionRepository.deleteById(transaction.getId())) {
            throw new EntityNotFoundException("Transaksi", id);
        }
    }

    /**
     * Mengubah keterangan, jumlah, jenis, dan/atau bukti transaksi milik pengguna.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            String amountText,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        if (description != null) {
            transaction.changeDescription(requireDescription(description));
        }
        if (amountText != null) {
            transaction.changeAmount(parseAmount(amountText));
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Menghitung ringkasan saldo seorang pengguna: pemasukan menambah, pengeluaran mengurangi. */
    public BalanceSummary getBalance(int userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        long income = transactions.stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
        long expense = transactions.stream()
                .filter(transaction -> !transaction.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
        return new BalanceSummary(transactions.size(), income, expense);
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik pengguna ini");
        }
        return transaction;
    }

    private String requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long amount;
        try {
            amount = Long.parseLong(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return amount;
    }
}
