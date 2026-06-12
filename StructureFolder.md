app/src/main/java/com/mobileprogramming/finsheet/
│
├─ core/                             # Utilitas global, extension, dan bumbu dapur app
│  ├─ network/                       # Network observer (FR-03), Ktor/Retrofit client (FR-09)
│  │  ├─ NetworkStatusTracker.kt
│  │  └─ ExchangeRateClient.kt
│  ├─ theme/                         # Jetpack Compose Design System (Color, Type, Theme)
│  ├─ utils/                         # Extension functions, Currency Formatter, DateUtils
│  └─ voicemail/                     # Audio recorder wrapper untuk voice-to-text (FR-06)
│
├─ data/                             # LAYER DATA (Implementasi Data, API, DB, OAuth)
│  ├─ auth/                          # Manajemen Google Sign-In & OAuth 2.0 (FR-01)
│  │  └─ GoogleAuthRepositoryImpl.kt
│  ├─ local/                         # Database Lokal Perangkat (FR-02)
│  │  ├─ db/
│  │  │  ├─ AppDatabase.kt
│  │  │  ├─ TransactionDao.kt        # CRUD query transaksi
│  │  │  └─ SubscriptionDao.kt       # Query untuk tagihan rutin (FR-07)
│  │  └─ entity/
│  │     ├─ TransactionEntity.kt     # Skema tabel lokal (total, kategori, foto, dll)
│  │     └─ SubscriptionEntity.kt    # Skema tabel tagihan rutin
│  ├─ remote/                        # Integrasi pihak ketiga (Google Sheets & Exchange Rate)
│  │  ├─ google/
│  │  │  └─ GoogleSheetsApiService.kt # HTTP call ke Sheets API via OAuth (FR-04)
│  │  └─ exchange/
│  │     └─ ExchangeRateApi.kt       # Fetch kurs mata uang (FR-09)
│  ├─ repository/                    # Implementasi dari kontrak domain repository
│  │  ├─ TransactionRepositoryImpl.kt
│  │  ├─ BudgetRepositoryImpl.kt
│  │  └─ ExchangeRateRepositoryImpl.kt
│  └─ worker/                        # WorkManager untuk Background Services
│     ├─ SyncWorker.kt               # Sinkronisasi otomatis ke Google Sheets (FR-04)
│     └─ SubscriptionScheduler.kt    # Cek & eksekusi tagihan rutin harian (FR-07)
│
├─ domain/                           # LAYER DOMAIN (Aturan Bisnis Murni - Bebas Framework)
│  ├─ model/                         # Data Class murni yang dibaca oleh UI
│  │  ├─ Transaction.kt
│  │  ├─ Budget.kt
│  │  └─ Subscription.kt
│  ├─ repository/                    # Kontrak / Interface (Abstraksi data)
│  │  ├─ GoogleAuthRepository.kt
│  │  ├─ TransactionRepository.kt
│  │  ├─ BudgetRepository.kt
│  │  └─ ExchangeRateRepository.kt
│  └─ usecase/                       # USECASE (Satu class = Satu fungsi spesifik/FR)
│     ├─ auth/
│     │  └─ SignInWithGoogleUseCase.kt  # (FR-01)
│     ├─ transaction/
│     │  ├─ GetTransactionsUseCase.kt   # (FR-02)
│     │  ├─ UpsertTransactionUseCase.kt # Gabungan Create/Update + Validasi Anggaran (FR-08)
│     │  └─ DeleteTransactionUseCase.kt
│     ├─ analytics/
│     │  └─ GetAnalyticsDataUseCase.kt  # Filter harian, mingguan, bulanan (FR-05)
│     ├─ budget/
│     │  └─ CheckBudgetLimitUseCase.kt  # Trigger warning jika over-budget (FR-08)
│     └─ voice/
│        └─ ParseVoiceToTransactionUseCase.kt # Ekstraksi teks suara ke objek data (FR-06)
│
├─ ui/                               # LAYER PRESENTATION (Jetpack Compose UI)
│  ├─ components/                    # Reusable Jetpack Compose Components (Global)
│  │  ├─ TransactionCard.kt
│  │  ├─ CustomTextField.kt
│  │  └─ LineChartView.kt            # Grafik untuk FR-05
│  ├─ features/                      # UI dipecah per-fitur layar
│  │  ├─ auth/
│  │  │  └─ LoginScreen.kt
│  │  ├─ dashboard/
│  │  │  ├─ DashboardScreen.kt
│  │  │  └─ DashboardViewModel.kt     # Berkomunikasi dengan Usecase
│  │  ├─ transaction_entry/
│  │  │  ├─ AddEditTransactionScreen.kt # Input transaksi + kamera foto + voice button
│  │  │  └─ TransactionEntryViewModel.kt
│  │  ├─ analytics/
│  │  │  ├─ AnalyticsScreen.kt       # Tampilan grafik (FR-05)
│  │  │  └─ AnalyticsViewModel.kt
│  │  └─ subscriptions/
│  │     ├─ SubscriptionScreen.kt    # Daftar tagihan rutin (FR-07)
│  │     └─ SubscriptionViewModel.kt
│  └─ navigation/                    # Type-Safe Navigation setup
│     ├─ Destinations.kt             # Definisi rute berbasis Object/Class
│     └─ NavGraph.kt                 # NavHost dan aturan transisi layar
│
└─ FinSheetApplication.kt            # Entry point aplikasi (Inisialisasi WorkManager/DI)