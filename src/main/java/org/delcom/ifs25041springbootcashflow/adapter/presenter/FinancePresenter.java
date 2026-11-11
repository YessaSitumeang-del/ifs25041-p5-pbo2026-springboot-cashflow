package org.delcom.ifs25041springbootcashflow.adapter.presenter;

import org.delcom.ifs25041springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25041springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan (format uang, label, pesan) dari entity,
 * use case, dan view sehingga format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<SortOption, String> SORT_LABELS = new EnumMap<>(Map.of(
            SortOption.AMOUNT_ASC, "Jumlah: terkecil ke terbesar",
            SortOption.AMOUNT_DESC, "Jumlah: terbesar ke terkecil",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    ));

    /** Memformat uang: pemisah ribuan koma dan dua desimal, mis. {@code Rp 1,000,000.00}. */
    public String formatAmount(long amount) {
        String body = String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount).abs());
        return amount < 0 ? "-" + body : body;
    }

    /** Label jenis transaksi untuk ditampilkan. */
    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label opsi pengurutan untuk ditampilkan. */
    public String sortLabel(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Memformat satu transaksi menjadi baris teks: {@code [id] keterangan | Rp x | [Jenis]}. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatAmount(transaction.getAmount()),
                typeLabel(transaction.getType())
        );
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo, mis. {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatAmount(balance);
    }

    /** Memformat ringkasan saldo dalam satu baris. */
    public String formatSummary(BalanceSummary summary) {
        return String.format(
                "Transaksi: %d | Pemasukan: %s | Pengeluaran: %s | %s",
                summary.count(),
                formatAmount(summary.totalIncome()),
                formatAmount(summary.totalExpense()),
                formatBalance(summary.balance())
        );
    }

    /** Memformat daftar transaksi menjadi teks multi-baris; pesan kosong jika tidak ada data. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil mencatat " + typeLabel(transaction.getType()).toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan sukses setelah mengubah transaksi. */
    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    /** Pesan gagal saat mengubah transaksi. */
    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
