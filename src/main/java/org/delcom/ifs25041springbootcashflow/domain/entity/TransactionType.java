package org.delcom.ifs25041springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan: pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    INCOME(1),
    EXPENSE(-1);

    /** Pengali saldo: +1 untuk pemasukan, -1 untuk pengeluaran. */
    private final int sign;

    TransactionType(int sign) {
        this.sign = sign;
    }

    public int sign() {
        return sign;
    }
}
