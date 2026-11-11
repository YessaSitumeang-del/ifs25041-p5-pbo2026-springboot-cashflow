# ifs25041-p5-pbo2026-springboot-cashflow

Aplikasi web pencatat transaksi keuangan pribadi berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder | `ifs25041-p5-pbo2026-springboot-cashflow` |
| Artifact Maven | `ifs25041-p5-pbo2026-springboot-cashflow` |
| Package | `org.delcom.ifs25041springbootcashflow` |
| Database | `pbo2026_cashflow` |

## Prasyarat

- JDK 25
- PostgreSQL — buat database terlebih dahulu:

```sql
CREATE DATABASE pbo2026_cashflow;
```

- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git):

```env
APP_PORT=8080

DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=pbo2026_cashflow
DB_USERNAME=postgres
DB_PASSWORD=postgres

ROOT_USERNAME=admin
ROOT_PASSWORD=admin

UPLOAD_DIR=uploads
```

Akun ADMIN di-seed otomatis saat start dari `ROOT_USERNAME` / `ROOT_PASSWORD`. Upload (foto profil dan bukti transaksi) disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`.

`DotEnv` hanya mengisi system property jika variabel lingkungan / property dengan nama yang sama **belum** ada. Di server/cloud, nilai `DB_*` dari environment platform yang dipakai.

## Menjalankan

```powershell
.\mvnw.cmd spring-boot:run
```

Buka `http://localhost:8080` (sesuai `APP_PORT`).

Test + cek Jacoco 100% (view dan auth dikecualikan):

```powershell
.\mvnw.cmd clean verify
```

Unit test memakai profil `test` (H2 in-memory mode PostgreSQL), bukan PostgreSQL.

## Fitur

- Register, login, logout; sesi bertahan setelah refresh (cookie `cashflow_uid` + `LOGGED_OUT_KEY`), password di-hash SHA-256.
- Role `ADMIN` / `USER`.
- Catat **Pemasukan** (`INCOME`) dan **Pengeluaran** (`EXPENSE`) dengan bukti gambar opsional (`proofPath`).
- Kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, saldo. Format uang `Rp 1,000,000.00`.
- Cari keterangan (case-insensitive), urut lewat header kolom grid atau pilihan `SortOption` (jumlah naik/turun, pemasukan dulu, pengeluaran dulu).
- Klik bukti untuk zoom (`ImagePreviewDialog`).
- Hapus dengan dialog konfirmasi, hanya data milik sendiri.
- Profil: nama, password, foto.
- ADMIN: daftar pengguna + transaksi pengguna lain (read-only); ADMIN tetap punya cashflow sendiri.
- Validasi: keterangan kosong, jumlah non-positif / non-numerik, ID tidak valid.

## Rute dan hak akses

| Kategori | Path | View | Akses |
| --- | --- | --- | --- |
| Public | `/login` | `LoginView` | Anonim; jika sudah login ke `/` |
| Public | `/register` | `RegisterView` | Anonim; jika sudah login ke `/` |
| Public | `/uploads/**` | `FileStorage` via Spring MVC | Static |
| Public | `/icons/**`, `styles.css` | Asset | Static |
| Protected | `/` | `FinanceView` | USER & ADMIN |
| Protected | `/profile` | `ProfileView` | USER & ADMIN |
| Admin | `/users` | `UsersView` | ADMIN saja; USER dialihkan ke `/` |
| Admin | `/user-transactions/{userId}` | `UserTransactionsView` | ADMIN saja (read-only); USER dialihkan ke `/` |

Rute terproteksi tanpa sesi dialihkan ke `/login` oleh `MainLayout`.

## Struktur

```
src/main/java/org/delcom/ifs25041springbootcashflow/
├── domain/
│   ├── PasswordHasher.java
│   ├── entity/          # Transaction, TransactionType, BalanceSummary, User, Role, SortOption
│   ├── exception/       # DomainException, EntityNotFoundException, ValidationException
│   └── repository/      # IRepository<T, ID>, ITransactionRepository, IUserRepository
├── usecase/             # AuthUseCase, FinanceUseCase, UserUseCase
├── adapter/
│   ├── presenter/       # FinancePresenter
│   ├── repository/      # InMemoryRepository, TransactionRepository, UserRepository,
│   │                    # JdbcTransactionRepository, JdbcUserRepository, SchemaInitializer
│   └── storage/         # FileStorage
├── framework/
│   ├── auth/            # AuthSession
│   └── view/            # Login, Register, MainLayout, Finance, Profile, Users, UserTransactions, ...
├── config/AppConfig.java
├── DotEnv.java
└── App.java
```

Dependency Rule: `domain` ← `usecase` ← `adapter` / `framework`. Domain dan use case tidak mengimpor Vaadin maupun Spring.

## CI/CD

`Jenkinsfile` (bawaan, tidak diubah): Checkout, Build & Test (Jacoco 100%), Trivy Security Scan, Package, Publish, Deploy.
