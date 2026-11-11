package org.delcom.ifs25041springbootcashflow.adapter.presenter;

import org.delcom.ifs25041springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25041springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25041springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25041springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private static Transaction income() {
        return new Transaction(1, 1, "Gaji", 1_000_000L, TransactionType.INCOME,
                null, LocalDateTime.of(2026, 3, 4, 5, 6));
    }

    private static Transaction expense() {
        return new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE,
                null, LocalDateTime.of(2026, 3, 4, 5, 6));
    }

    @Test
    void formatsMoneyWithThousandsAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatAmount(0L));
        assertEquals("Rp 4,975,000.00", presenter.formatAmount(4_975_000L));
        assertEquals("-Rp 25,000.00", presenter.formatAmount(-25_000L));
    }

    @Test
    void formatsLabels() {
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));
        for (SortOption option : SortOption.values()) {
            assertNotNull(presenter.sortLabel(option));
        }
    }

    @Test
    void formatsRowAndTimestamp() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income()));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense()));
        assertEquals("04/03/2026 05:06", presenter.formatCreatedAt(income()));
    }

    @Test
    void formatsBalanceAndSummary() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000L));
        String summary = presenter.formatSummary(new BalanceSummary(2, 5_000_000L, 25_000L));
        assertEquals(
                "Transaksi: 2 | Pemasukan: Rp 5,000,000.00 | Pengeluaran: Rp 25,000.00 | Saldo: Rp 4,975,000.00",
                summary
        );
    }

    @Test
    void formatsLists() {
        String all = presenter.showTransactions(List.of(income(), expense()));
        assertTrue(all.startsWith("Daftar Transaksi:"));
        assertTrue(all.contains("[1] Gaji"));
        assertTrue(all.contains("[2] Makan"));

        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", presenter.showTransactions(List.of()));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "x"));
        assertTrue(presenter.showSearchResults(List.of(income()), "gaji").contains("[1] Gaji"));
        assertEquals("Daftar Transaksi (Terurut):\n- Data transaksi belum tersedia!",
                presenter.showSortedTransactions(List.of()));
        assertTrue(presenter.showSortedTransactions(List.of(expense())).contains("[2] Makan"));
    }

    @Test
    void formatsMessages() {
        assertEquals("Berhasil mencatat pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income()));
        assertEquals("Berhasil mencatat pengeluaran: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddSuccess(expense()));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 5.", presenter.showUpdateFailed(5));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Jumlah harus berupa angka", presenter.showValidationError("Jumlah harus berupa angka"));
    }
}
