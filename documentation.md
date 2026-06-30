1. 

# Dokumentasi Integrasi & Perbaikan FinSheet

Dokumen ini berisi rangkuman langkah-langkah yang telah dilakukan pada proyek FinSheet. Dokumentasi ini dibuat sebagai bahan pembelajaran untuk mengetahui apa saja masalah yang diselesaikan dan bagaimana cara penyelesaiannya.

## 1. Perbaikan Splash Screen (Logo Terpotong)

**Masalah:**
Logo aplikasi pada *splash screen* terpotong oleh bentuk *masking* lingkaran bawaan Android (khususnya Android 12+), namun jika diperkecil, logo di tampilan *launcher* aplikasi menjadi terlalu kecil.

**Penyelesaian:**
Kita menggunakan pendekatan dengan memisahkan *drawable* untuk *launcher* dan *splash screen*.

1. **`ic_launcher_foreground.xml`**: Tetap diatur dengan *inset* normal (contoh: `24dp`) agar logo di *launcher screen* (beranda HP) tetap proporsional.
2. **`ic_splash_logo.xml`**: Membuat *drawable* baru khusus untuk *splash screen* dengan *inset* yang lebih besar (contoh: `40dp`). Ini memberikan ruang kosong (margin) yang cukup sehingga saat di-*masking* bentuk lingkaran oleh sistem, logo tidak terpotong.
3. **`themes.xml` (dan `themes-night.xml`)**: Mengubah referensi `windowSplashScreenAnimatedIcon` untuk menunjuk ke `@drawable/ic_splash_logo` yang baru saja kita buat.

## 2. Integrasi Login dengan Akun Google

**Tujuan:**
Memberikan kemudahan kepada pengguna untuk masuk/login ke dalam aplikasi FinSheet menggunakan akun Google mereka melalui Firebase Authentication dan Credential Manager API (cara modern di Android).

**Langkah-langkah Penyelesaian:**

### A. Persiapan dan Konfigurasi Firebase

1. **`google-services.json`**: Menambahkan file konfigurasi dari Firebase Console ke dalam direktori `app/`. File ini berisi kunci dan Client ID untuk menghubungkan aplikasi dengan project Firebase.
2. **`build.gradle.kts` (App)**: Menambahkan *plugin* `com.google.gms.google-services` dan *dependencies* penting seperti:
   - `firebase-auth` (Untuk autentikasi di server Firebase).
   - `androidx.credentials` & `androidx.credentials-play-services-auth` (API modern Android untuk otorisasi Google).
   - `googleid` (Untuk membuat permintaan login ID Google).

### B. Pembuatan Klien Autentikasi (`GoogleAuthClient.kt`)

Membuat kelas pembantu yang menangani logika rumit login Google:

1. **Credential Manager**: Menginisialisasi `CredentialManager.create(context)`.
2. **Nonce**: Membuat *hashed nonce* (token acak) untuk mencegah serangan *replay attacks*. *Nonce* ini diteruskan ke Google saat meminta token.
3. **GetCredentialRequest**: Membangun permintaan login (menggunakan `GetGoogleIdOption`) yang memunculkan *bottom sheet* (dialog) pilihan akun Google di layar.
4. **Firebase Auth**: Setelah mendapat *Google ID Token*, token tersebut dikirimkan ke `GoogleAuthProvider.getCredential()` lalu melakukan `auth.signInWithCredential(credential)` untuk mencatat user di sistem Firebase.

### C. Pembuatan Antarmuka Pengguna (`LoginScreen.kt`)

Membuat halaman UI dengan Jetpack Compose yang berisi:

1. Logo aplikasi dan pesan selamat datang.
2. Tombol **"Sign in with Google"**.
3. Penanganan *loading state* (muncul `CircularProgressIndicator` saat proses loading).
4. Pemanggilan *coroutine* untuk menjalankan fungsi `signIn()` dari `GoogleAuthClient` ketika tombol ditekan, dan jika berhasil akan memanggil fungsi navigasi menuju *Dashboard*.

### D. Pengaturan Navigasi (`Screen.kt` & `FinSheetNavGraph.kt`)

1. **`Screen.kt`**: Menambahkan objek baru `Screen.Login` ke dalam *sealed class* rute navigasi.
2. **`FinSheetNavGraph.kt`**: Menambahkan fungsi `composable<Screen.Login>` ke dalam grafik rute, yang menampilkan komponen `LoginScreen` dan memberi instruksi kemana aplikasi harus berpindah setelah login sukses (menuju `Screen.Dashboard` sembari menghapus layar login dari *backstack* memori agar tidak bisa kembali ke layar login).

### E. Pemeriksaan Sesi Otomatis (`MainActivity.kt`)

Memodifikasi entri poin aplikasi agar cerdas dalam menentukan layar mana yang pertama kali dibuka:

1. Menggunakan `FirebaseAuth.getInstance().currentUser`.
2. Jika *user* sudah pernah login sebelumnya (tidak bernilai `null`), variabel `startDestination` akan diarahkan ke `Screen.Dashboard`.
3. Jika *user* belum login, maka diatur ke `Screen.Login`.
4. Nilai `startDestination` ini dilempar ke `FinSheetNavGraph` agar grafik navigasi langsung membuka layar yang tepat.

---

*Dokumentasi ini mencatat perubahan teknis utama yang memastikan aplikasi memiliki alur login yang mulus dan bebas dari masalah UI.*
