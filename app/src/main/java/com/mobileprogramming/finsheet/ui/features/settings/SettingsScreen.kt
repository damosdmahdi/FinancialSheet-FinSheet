package com.mobileprogramming.finsheet.ui.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.mobileprogramming.finsheet.ui.features.auth.GoogleAuthClient
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import java.io.File
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = com.mobileprogramming.finsheet.di.Injection.provideSettingsViewModelFactory(
            LocalContext.current.applicationContext
        )
    ),
    onNavigateBack: () -> Unit,
    onNavigateToBeranda: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToAnggaran: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profileBitmap = remember(uiState.customProfilePhotoPath) {
        uiState.customProfilePhotoPath?.let { path ->
            try {
                android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshSettings()
    }

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val cameraImageUri = remember {
        val imageDir = File(context.cacheDir, "images").also { it.mkdirs() }
        val imageFile = File.createTempFile("camera_profile_", ".jpg", imageDir)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.saveCustomProfilePhoto(context, cameraImageUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(cameraImageUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.saveCustomProfilePhoto(context, uri)
        }
    }

    fun launchCamera() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val authClient = remember {
        GoogleAuthClient(
            context = context,
            auth = FirebaseAuth.getInstance()
        )
    }

    var showCurrencyDialog by remember { mutableStateOf(false) }

    val shareAppLauncher = {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "FinSheet - Aplikasi Manajemen Keuangan Pribadi")
            putExtra(android.content.Intent.EXTRA_TEXT, "Halo! Cobain deh FinSheet, aplikasi manajemen keuangan pribadi yang keren banget! Link download: https://github.com/damosdmahdi/PPB_tubes")
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Bagikan FinSheet melalui:"))
    }

    // Hubungi Muhammad Irfan Wira Kusuma sebagai developer utama
    val contactUsLauncher = {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:irfan.wk.2705@gmail.com")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Pertanyaan / Feedback FinSheet")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Tidak ada aplikasi email yang ditemukan", android.widget.Toast.LENGTH_SHORT).show()
        }
    }



    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Text(
                    text = "Pilih Mata Uang Utama",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currencies = listOf("IDR", "USD", "EUR", "JPY", "SGD")
                    currencies.forEach { curr ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(curr)
                                    showCurrencyDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.selectedCurrency == curr) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = curr,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (uiState.selectedCurrency == curr) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (uiState.selectedCurrency == curr) 
                                        FontWeight.Bold 
                                    else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showPhotoSourceDialog) {
        val primaryBlue = Color(0xFF1A5BEB)
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = {
                Text(
                    text = "Pilih Sumber Foto",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Kamera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(primaryBlue.copy(alpha = 0.08f))
                            .clickable {
                                showPhotoSourceDialog = false
                                launchCamera()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Kamera",
                            tint = primaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Ambil Foto (Kamera)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = primaryBlue
                        )
                    }
                    // Galeri
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showPhotoSourceDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Photo,
                            contentDescription = "Galeri",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Pilih dari Galeri",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        bottomBar = {
            com.mobileprogramming.finsheet.ui.components.BottomNavigationBar(
                selectedItem = "Setelan",
                onBerandaClick = onNavigateToBeranda,
                onTransaksiClick = onNavigateToTransaksi,
                onFabClick = onNavigateToAddTransaction,
                onAnggaranClick = onNavigateToAnggaran,
                onSettingsClick = { /* Do nothing */ }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kelola akun dan preferensi aplikasi Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Account Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isUserLoggedIn) Color(0xFF2E7D32) 
                                    else Color(0xFF757575)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STATUS AKUN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isUserLoggedIn) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { showPhotoSourceDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.userPhotoUrl != null) {
                                    AsyncImage(
                                        model = uiState.userPhotoUrl,
                                        contentDescription = "Foto Profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (profileBitmap != null) {
                                    Image(
                                        bitmap = profileBitmap,
                                        contentDescription = "Foto Profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val initial = (uiState.userDisplayName?.takeIf { it.isNotBlank() }
                                        ?: uiState.userEmail?.takeIf { it.isNotBlank() }
                                        ?: "F").first().uppercase()
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = uiState.userDisplayName ?: "Pengguna FinSheet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.userEmail ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                viewModel.signOut()
                                android.widget.Toast.makeText(context, "Berhasil keluar dari akun", android.widget.Toast.LENGTH_SHORT).show()
                                onNavigateToLogin()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keluar dari Akun",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else if (uiState.isGuestMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = com.mobileprogramming.finsheet.R.drawable.anime_girl_commander),
                                    contentDescription = "Avatar Tamu",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = uiState.userDisplayName ?: "Tamu Finshett (Honoratus)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.userEmail ?: "Mode Guest",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = authClient.signIn()
                                    if (result != null) {
                                        android.widget.Toast.makeText(context, "Sign In Successful", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Sign In Failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE0E0E0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = androidx.compose.ui.graphics.Color.White,
                                contentColor = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = com.mobileprogramming.finsheet.R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Masuk dengan Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { showPhotoSourceDialog = true }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileBitmap != null) {
                                    Image(
                                        bitmap = profileBitmap,
                                        contentDescription = "Foto Profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Masuk atau Daftar",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Masuk untuk sinkronisasi data Anda",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = authClient.signIn()
                                    if (result != null) {
                                        android.widget.Toast.makeText(context, "Sign In Successful", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Sign In Failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE0E0E0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = androidx.compose.ui.graphics.Color.White,
                                contentColor = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = com.mobileprogramming.finsheet.R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Masuk dengan Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                                )
                            }
                        }
                    }
                }
            }
            
            // Standard Cards
            SettingsItemCard(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TableView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "Akses Spreadsheet",
                subtitle = "Buka data transaksi di Google Sheets",
                onClick = {
                    if (!uiState.isUserLoggedIn) {
                        android.widget.Toast.makeText(
                            context,
                            "Harap login dengan Google terlebih dahulu untuk mengakses spreadsheet!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else if (!uiState.hasSyncedSpreadsheet) {
                        android.widget.Toast.makeText(
                            context,
                            "Harap lakukan sinkronisasi data transaksi terlebih dahulu di menu Transaksi!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://docs.google.com/spreadsheets")
                        ).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Tidak dapat membuka Google Sheets", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            SettingsItemCard(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.selectedCurrency == "IDR") "Rp" else "$",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = "Mata Uang",
                subtitle = getCurrencyDisplayName(uiState.selectedCurrency),
                onClick = { showCurrencyDialog = true },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // DUKUNGAN
            Text(
                text = "DUKUNGAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )

            SettingsItemCard(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "Hubungi Kami",
                subtitle = "Kirim pertanyaan atau masukan",
                onClick = contactUsLauncher
            )

            SettingsItemCard(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "Bagikan Aplikasi",
                subtitle = "Ajak teman menggunakan FinSheet",
                onClick = shareAppLauncher
            )

            // NOTIFIKASI ANGGARAN
            Text(
                text = "NOTIFIKASI ANGGARAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )

            SettingsSimpleCard(
                title = "Anggaran Harian Terlewati",
                trailing = {
                    Switch(
                        checked = uiState.anggaranHarian,
                        onCheckedChange = { viewModel.setAnggaranHarian(it) }
                    )
                }
            )

            SettingsSimpleCard(
                title = "Anggaran Mingguan Terlewati",
                trailing = {
                    Switch(
                        checked = uiState.anggaranMingguan,
                        onCheckedChange = { viewModel.setAnggaranMingguan(it) }
                    )
                }
            )

            SettingsSimpleCard(
                title = "Anggaran Bulanan Terlewati",
                trailing = {
                    Switch(
                        checked = uiState.anggaranBulanan,
                        onCheckedChange = { viewModel.setAnggaranBulanan(it) }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItemCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

private fun getCurrencyDisplayName(code: String): String {
    return when (code) {
        "IDR" -> "Indonesian Rupiah"
        "USD" -> "US Dollar"
        "EUR" -> "Euro"
        "JPY" -> "Japanese Yen"
        "SGD" -> "Singapore Dollar"
        else -> code
    }
}

@Composable
fun ShanksEyeIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw Eye Shape (White base)
        val eyePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.2f, w * 0.85f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.8f, w * 0.15f, h * 0.5f)
            close()
        }
        drawPath(
            path = eyePath,
            color = Color.White,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        drawPath(
            path = eyePath,
            color = Color(0xFF1E1E24),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Draw Iris (Red circle in the center)
        drawCircle(
            color = Color(0xFFC62828),
            radius = w * 0.18f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
        )
        // Pupil (Black)
        drawCircle(
            color = Color.Black,
            radius = w * 0.08f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
        )

        // Draw 3 vertical red scars (Shanks' iconic scars) crossing the eye
        val scarColor = Color(0xFFD32F2F)
        val scarWidth = 2.5.dp.toPx()
        
        // Scar 1
        drawLine(
            color = scarColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.12f),
            end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.88f),
            strokeWidth = scarWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Scar 2
        drawLine(
            color = scarColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.51f, h * 0.08f),
            end = androidx.compose.ui.geometry.Offset(w * 0.47f, h * 0.92f),
            strokeWidth = scarWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Scar 3
        drawLine(
            color = scarColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.12f),
            end = androidx.compose.ui.geometry.Offset(w * 0.54f, h * 0.88f),
            strokeWidth = scarWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun SettingsSimpleCard(
    title: String,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            trailing()
        }
    }
}
