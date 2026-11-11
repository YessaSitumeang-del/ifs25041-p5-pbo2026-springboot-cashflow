package org.delcom.ifs25041springbootcashflow.domain.entity;

/**
 * Ringkasan saldo seorang pengguna: jumlah transaksi, total pemasukan, dan total pengeluaran.
 *
 * @param count        banyaknya transaksi
 * @param totalIncome  total seluruh pemasukan
 * @param totalExpense total seluruh pengeluaran (nilai positif)
 */
public record BalanceSummary(int count, long totalIncome, long totalExpense) {

    /** Saldo = pemasukan dikurangi pengeluaran. */
    public long balance() {
        return totalIncome - totalExpense;
    }
}
