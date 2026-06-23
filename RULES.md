# FinSheet - Architecture & Development Rules

Dokumen ini adalah **Single Source of Truth (SSOT)** untuk pengembangan aplikasi Android "FinSheet". Semua anggota tim Front-End dan AI Assistant **WAJIB** mematuhi batasan arsitektur di bawah ini tanpa pengecualian.

## 1. Target Folder Structure (Clean Architecture)
Struktur di dalam `app/src/main/java/com/mobileprogramming/finsheet/` harus dipertahankan dan dikembangkan dengan pakem berikut:

```text
├── core/                 # Komponen independen yang bisa dipakai semua layer
│   ├── network/          # Konfigurasi HTTP Client (Retrofit/Ktor), NetworkStatusTracker
│   ├── theme/            # Compose Theme, Color, Typography
│   └── utils/            # Extension functions, konstanta global
├── data/                 # Segala hal terkait pengambilan/penyimpanan data
│   ├── auth/             # Implementasi Firebase Auth
│   ├── local/            # SQLite/Room Database (Entity, DAO, Database Provider)
│   ├── remote/           # Money Exchange API (DTO, API Services)
│   ├── repository/       # Implementasi dari Interfaces Repository yang ada di Domain
│   └── worker/           # WorkManager (Background tasks, sync data)
├── domain/               # Aturan Bisnis murni, tidak boleh ada dependensi Android/UI di sini
│   ├── model/            # Plain Kotlin Data Class (Entity bisnis)
│   ├── repository/       # Interface (Kontrak) Repository
│   └── usecase/          # Interaktor bisnis (e.g., GetConvertedCurrencyUseCase)
├── ui/                   # Layer UI murni
│   ├── components/       # Reusable Compose UI (Button, Card, Chart, CameraPreview)
│   ├── features/         # Screen utama per modul (Dashboard, Auth, AddTransaction)
│   └── navigation/       # Jetpack Navigation Compose (NavGraph, Routes)
└── MainActivity.kt       # Entry point aplikasi
```

## 2. Component Boundaries (Batas Wewenang Layer)
*Pelanggaran batas-batas ini akan menyebabkan penolakan PR / regenerasi kode.*

*   **Layer `data` (Data Layer)**:
    *   **Tugas**: Mengambil data dari jaringan (API), menyimpannya ke database (SQLite/Room), autentikasi Firebase, dan penjadwalan lokal (WorkManager).
    *   **Larangan**: Tidak boleh mengembalikan kelas DTO jaringan atau Entity Database ke layer UI. Harus selalu dipetakan (*mapping*) menjadi `domain/model` sebelum di-_return_.
*   **Layer `domain` (Domain Layer)**:
    *   **Tugas**: Berisi UseCases yang menangani logika bisnis absolut (misal kalkulasi konversi mata uang).
    *   **Larangan**: Dilarang mengimpor atau menggunakan komponen bawaan Android (`Context`, `Activity`, dll) atau Jetpack Compose. Murni Kotlin (*Pure Kotlin*).
*   **Layer `ui` (UI Layer)**:
    *   **Tugas**: Menerima interaksi pengguna, memegang status layar (*state management*), dan merender UI Compose.
    *   **Larangan**: Tidak boleh ada pemanggilan API langsung, *query database*, atau instansiasi *WorkManager* langsung di dalam `ViewModel` atau *Composable*. Semua harus lewat `UseCase` (di-inject via constructor).

## 3. Tech Stack & Integration Rules
Implementasi dari fitur spesifik FinSheet harus dikerjakan dengan standar ini:

1.  **State Management (UI)**:
    *   Selalu gunakan `ViewModel` untuk menyimpan state. 
    *   Ekspos status UI menggunakan `StateFlow` (e.g., `private val _uiState = MutableStateFlow(...)`).
    *   Di Compose, baca state dengan pola: `val uiState by viewModel.uiState.collectAsState()`.
2.  **Navigation**:
    *   Hanya gunakan Jetpack Navigation Compose di dalam paket `ui/navigation/`. Jangan passing `NavController` masuk ke dalam layar Compose fitur. Gunakan *callback/lambda* (e.g., `onNavigateToHome: () -> Unit`).
3.  **CRUD SQLite (Room)**:
    *   DAO hanya hidup di dalam `data/local/db`. Semua operasi harus dijalankan secara asinkron (*Coroutines* / *suspend functions* / *Flow*).
4.  **Money Exchange API**:
    *   Pemanggilan eksternal harus ditangani di `data/remote/`. Lakukan mekanisme pengamanan seperti penanganan *error response* dan _network connectivity parsing_ di layer ini.
5.  **Camera Sensor**:
    *   Gunakan CameraX. Pembungkus tampilan kameranya masuk ke `ui/components/`, sedangkan logika pemrosesan gambar atau penyimpanan filenya harus didelegasikan ke *use-cases* melalui *repository*.
6.  **Firebase Auth**:
    *   Tidak boleh diimplementasikan langsung di layar/UI. Implementasi spesifik (`FirebaseAuth.getInstance()`) dibungkus ke dalam `data/auth/AuthRepositoryImpl` yang memenuhi *interface* di domain layer.
7.  **WorkManager**:
    *   File *worker* diletakkan di `data/worker/`. Pemicunya (enqueue) dilakukan melalui *UseCase*, bukan melalui `Activity` atau `Fragment/Compose`.

## 4. Migration Strategy (Fragment/XML -> Compose)
Jika di masa depan ditemukan *legacy layout* atau XML yang tersisa:
1.  **Jangan modifikasi XML-nya.** Biarkan saja jika masih *exist*.
2.  Buat file baru berekstensi `.kt` di `ui/features/<namamodul>/`.
3.  Ubah elemen XML UI menjadi Composable Functions yang dikelompokkan ke dalam struktur layar.
4.  Gantikan pola akses ViewModel yang sebelumnya menggunakan *LiveData* di Fragment menjadi *StateFlow* dengan injeksi `viewModel()` secara deklaratif di hirarki Composable.

## 5. Naming Conventions
*   **Kelas & Composable**: *PascalCase* (contoh: `AddTransactionScreen`, `BudgetProgressItem`). Composable function yang merepresentasikan layar harus diakhiri dengan sufiks `Screen`.
*   **Variabel, Fungsi & State**: *camelCase* (contoh: `uiState`, `fetchTransaction()`).
*   **UseCase**: Nama harus mendeskripsikan *action* dan diakhiri dengan sufiks `UseCase` (contoh: `GetExchangeRateUseCase`).
*   **Repository**: 
    *   Interface: `TransactionRepository` (di `domain/repository`)
    *   Implementasi: `TransactionRepositoryImpl` (di `data/repository`)
