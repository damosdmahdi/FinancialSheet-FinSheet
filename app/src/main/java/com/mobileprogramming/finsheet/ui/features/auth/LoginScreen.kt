package com.mobileprogramming.finsheet.ui.features.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.mobileprogramming.finsheet.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val authClient = remember {
        GoogleAuthClient(
            context = context,
            auth = FirebaseAuth.getInstance()
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == android.app.Activity.RESULT_OK) {
            coroutineScope.launch {
                isLoading = true
                val email = FirebaseAuth.getInstance().currentUser?.email
                if (email != null) {
                    try {
                        val token = authClient.getAccessToken(email)
                        if (token != null) {
                            val sheetsRepo = com.mobileprogramming.finsheet.data.remote.GoogleSheetsRepository(context)
                            val (id, isNew) = sheetsRepo.ensureSpreadsheetExists(token)
                            if (id != null) {
                                val message = if (isNew) "Spreadsheet baru berhasil dibuat!" else "Spreadsheet lama berhasil dihubungkan!"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Gagal! Pastikan API Google Sheets & Drive AKTIF di Cloud Console.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        if (e.message == "DRIVE_API_DISABLED") {
                            Toast.makeText(context, "GAGAL: Google Drive API belum diaktifkan di Cloud Console!", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("LoginScreen", "Error after permission granted", e)
                        }
                    }
                }
                isLoading = false
                onNavigateToDashboard()
            }
        } else {
            isLoading = false
            Toast.makeText(context, "Izin Google Drive ditolak", Toast.LENGTH_SHORT).show()
            onNavigateToDashboard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_finsheet),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome to FinSheet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign in to continue managing your finances",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Menyiapkan akun & Sinkronisasi...", fontSize = 14.sp)
            }
        } else {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val result = authClient.signIn()
                        if (result != null) {
                            val email = result.user?.email
                            if (email != null) {
                                try {
                                    val token = authClient.getAccessToken(email)
                                    if (token != null) {
                                        val sheetsRepo = com.mobileprogramming.finsheet.data.remote.GoogleSheetsRepository(context)
                                        val (id, isNew) = sheetsRepo.ensureSpreadsheetExists(token)
                                        if (id != null) {
                                            val message = if (isNew) "Spreadsheet baru berhasil dibuat!" else "Spreadsheet lama berhasil dihubungkan!"
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal! Pastikan API Google Sheets & Drive AKTIF di Cloud Console.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    isLoading = false
                                    onNavigateToDashboard()
                                } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                                    // Tampilkan persetujuan ke user
                                    e.intent?.let { intent ->
                                        launcher.launch(intent)
                                    } ?: run {
                                        isLoading = false
                                        Toast.makeText(context, "Tidak dapat meminta izin", Toast.LENGTH_SHORT).show()
                                        onNavigateToDashboard()
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    if (e.message == "DRIVE_API_DISABLED") {
                                        Toast.makeText(context, "GAGAL: Google Drive API belum diaktifkan di Cloud Console!", Toast.LENGTH_LONG).show()
                                    }
                                    onNavigateToDashboard()
                                }
                            } else {
                                isLoading = false
                                onNavigateToDashboard()
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Sign In Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Sign in with Google", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val result = authClient.signInAsGuest()
                        isLoading = false
                        if (result != null) {
                            Toast.makeText(context, "Masuk sebagai Guest Berhasil", Toast.LENGTH_SHORT).show()
                            onNavigateToDashboard()
                        } else {
                            Toast.makeText(context, "Gagal masuk sebagai Guest", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Masuk sebagai Guest", fontSize = 16.sp)
            }

        }
    }
}
