package com.mobileprogramming.finsheet.ui.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File
import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncResult
import com.mobileprogramming.finsheet.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileprogramming.finsheet.di.Injection
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper

private fun formatDateMillis(millis: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(
        factory = Injection.provideHistoryViewModelFactory(LocalContext.current.applicationContext)
    ),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToTransfer: (String) -> Unit = {},
    onNavigateToAnggaran: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /* ---- Local UI state ---- */
    var showDatePicker          by remember { mutableStateOf(false) }
    var isSyncing               by remember { mutableStateOf(false) }
    // Key diincrement saat Reset agar DateRangePickerState recreate (seleksi hilang)
    var datePickerKey           by remember { mutableIntStateOf(0) }

    // Rentang tanggal dari DateRangePicker
    val dateRangePickerState = key(datePickerKey) { rememberDateRangePickerState() }
    val dateRangeText = remember(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
    ) {
        val start = dateRangePickerState.selectedStartDateMillis
        val end   = dateRangePickerState.selectedEndDateMillis
        when {
            start != null && end != null && start != end ->
                "${formatDateMillis(start)} - ${formatDateMillis(end)}"
            start != null ->
                formatDateMillis(start)
            else ->
                "Pilih Rentang Waktu"
        }
    }

    val primaryBlue   = MaterialTheme.colorScheme.primary
    val incomeGreen   = Color(0xFF4CAF50)
    val expenseRed    = Color(0xFFF44336)
    val segmentedBg   = MaterialTheme.colorScheme.surfaceContainerHigh
    
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    var selectedTransactionToAction by remember { mutableStateOf<TransactionItemUI?>(null) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showDatePicker = false 
                    viewModel.setDateRange(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerKey++                               // reset local picker state
                        viewModel.setDateRange(null, null)            // hapus filter tanggal
                        viewModel.setFilter(TransactionFilter.SEMUA)  // kembalikan ke "Semua"
                    }) {
                        Text("Reset")
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Batal")
                    }
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text     = "Pilih Rentang Waktu",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style    = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                headline = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end   = dateRangePickerState.selectedEndDateMillis
                        Text(
                            text  = if (start != null) formatDateMillis(start) else "Tanggal mulai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (start != null)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("—", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text  = if (end != null) formatDateMillis(end) else "Tanggal selesai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (end != null)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                showModeToggle = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            )
        }
    }

    if (selectedTransactionToAction != null) {
        val item = selectedTransactionToAction!!
        val isLunas = item.status == "LUNAS"
        val label = if (item.transactionType == "DEBT") "Hutang" else "Piutang"
        
        if (isLunas) {
            AlertDialog(
                onDismissRequest = { selectedTransactionToAction = null },
                title = {
                    Text(text = "Batalkan Pelunasan", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = "Apakah Anda ingin membatalkan status lunas untuk catatan $label ini?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.undoLunas(item.id)
                            selectedTransactionToAction = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Ya, Batalkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTransactionToAction = null }) {
                        Text("Batal")
                    }
                }
            )
        } else {
            if (item.transactionType == "DEBT" && item.isDitalangin) {
                val accounts by viewModel.accounts.collectAsStateWithLifecycle()
                var selectedAccountId by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { selectedTransactionToAction = null },
                    title = {
                        Text(
                            text = "Lunasi Hutang (Ditalangi)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Pilih rekening yang digunakan untuk membayar hutang ini:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val isSelected = acc.id == selectedAccountId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            )
                                            .clickable {
                                                selectedAccountId = acc.id
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(CategoryIconMapper.getBackgroundColorByHex(acc.color)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = CategoryIconMapper.getIconByName(acc.icon),
                                                    contentDescription = null,
                                                    tint = CategoryIconMapper.getColorByHex(acc.color),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = acc.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedAccountId = acc.id }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (selectedAccountId != null) {
                                    viewModel.markAsLunas(item.id, selectedAccountId)
                                    selectedTransactionToAction = null
                                }
                            },
                            enabled = selectedAccountId != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ya, Lunas", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { selectedTransactionToAction = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batal")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            } else {
                AlertDialog(
                    onDismissRequest = { selectedTransactionToAction = null },
                    title = {
                        Text(text = "Lunasi $label", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(text = "Apakah Anda ingin menandai catatan $label ini sebagai lunas?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.markAsLunas(item.id, null)
                                selectedTransactionToAction = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Ya, Lunas")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedTransactionToAction = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Riwayat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.surface,
                    titleContentColor          = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onFabClick       = onNavigateToAddTransaction,
                onBerandaClick   = onNavigateToDashboard,
                onTransaksiClick = { /* sudah berada di layar ini */ },
                onAnggaranClick  = onNavigateToAnggaran,
                onSettingsClick  = onNavigateToSettings,
                selectedItem     = "Transaksi"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item(key = "header_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "Riwayat",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val context = LocalContext.current
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val prefs = remember(context) { context.getSharedPreferences("finsheet_prefs", android.content.Context.MODE_PRIVATE) }
                    
                    var hasManualSync by remember {
                        mutableStateOf(
                            !prefs.getString("apps_script_url", null).isNullOrBlank()
                        )
                    }

                    DisposableEffect(prefs) {
                        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                            if (key == "apps_script_url") {
                                hasManualSync = !prefs.getString("apps_script_url", null).isNullOrBlank()
                            }
                        }
                        prefs.registerOnSharedPreferenceChangeListener(listener)
                        onDispose {
                            prefs.unregisterOnSharedPreferenceChangeListener(listener)
                        }
                    }

                    val canSync = currentUser != null && (!currentUser.isAnonymous || hasManualSync)

                    SyncStatusChip(
                        isSyncing = isSyncing,
                        isUserLoggedIn = canSync,
                        primaryBlue = primaryBlue,
                        onClick   = {
                            if (!isSyncing) {
                                isSyncing = true
                                val email = currentUser?.email
                                if (email != null) {
                                    viewModel.syncToGoogleSheets(email) { result ->
                                        isSyncing = false
                                        when (result) {
                                            is SyncResult.Success -> {
                                                android.widget.Toast.makeText(context, "Berhasil Sinkron!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            is SyncResult.NoNewData -> {
                                                android.widget.Toast.makeText(context, "Sudah tersinkronisasi, tidak ada data baru.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            is SyncResult.TokenError -> {
                                                android.widget.Toast.makeText(context, "Izin belum diberikan, harap login ulang atau izinkan akses Drive.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            is SyncResult.SheetError -> {
                                                android.widget.Toast.makeText(context, "Gagal membuat/menemukan spreadsheet.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            is SyncResult.Error -> {
                                                android.widget.Toast.makeText(context, result.message ?: "Gagal Sinkron", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else if (currentUser != null && currentUser.isAnonymous && hasManualSync) {
                                    viewModel.syncToGoogleSheets("guest") { result ->
                                        isSyncing = false
                                        when (result) {
                                            is SyncResult.Success -> {
                                                android.widget.Toast.makeText(context, "Berhasil Sinkron (Manual)!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            is SyncResult.NoNewData -> {
                                                android.widget.Toast.makeText(context, "Sudah tersinkronisasi, tidak ada data baru.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            is SyncResult.TokenError -> {
                                                android.widget.Toast.makeText(context, "Token akses manual salah atau kedaluwarsa. Silakan perbarui di menu Tamu.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            is SyncResult.SheetError -> {
                                                android.widget.Toast.makeText(context, "Gagal membuat/menemukan spreadsheet. Periksa ID spreadsheet Anda.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                            is SyncResult.Error -> {
                                                android.widget.Toast.makeText(context, result.message ?: "Gagal Sinkron", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    isSyncing = false
                                    android.widget.Toast.makeText(context, "Harap login dengan Google atau atur akses Spreadsheet manual di menu Tamu (klik & tahan).", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }

            item(key = "date_range_selector") {
                DateRangeRow(
                    label       = dateRangeText,
                    onToggle    = { showDatePicker = true },
                    primaryBlue = primaryBlue,
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item(key = "filter_tabs") {
                FilterTabRow(
                    selected    = uiState.selectedFilter,
                    onSelect    = { viewModel.setFilter(it) },
                    primaryBlue = primaryBlue,
                    segmentedBg = segmentedBg,
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item(key = "loading_indicator") {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }
            } else if (uiState.groups.isEmpty()) {
                item(key = "empty_state") {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        val emptyText = when (uiState.selectedFilter) {
                            TransactionFilter.SEMUA -> "Belum ada transaksi atau transfer."
                            TransactionFilter.PENGELUARAN -> "Belum ada transaksi pengeluaran."
                            TransactionFilter.PEMASUKAN -> "Belum ada transaksi pemasukan."
                            TransactionFilter.TRANSFER -> "Belum ada transfer antar rekening."
                            TransactionFilter.HUTANG -> "Belum ada catatan hutang."
                            TransactionFilter.PIUTANG -> "Belum ada catatan piutang."
                        }
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                uiState.groups.forEachIndexed { index, group ->
                    item(key = "header_${group.dateLabel}_$index") {
                        Text(
                            text     = group.dateLabel,
                            style    = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical   = 6.dp
                            )
                        )
                    }

                    items(
                        items = group.items,
                        key   = { it.id } 
                    ) { item ->
                        when (item) {
                            is TransactionItemUI -> {
                                TransactionRow(
                                    item        = item,
                                    incomeGreen = incomeGreen,
                                    expenseRed  = expenseRed,
                                    onClick     = { onNavigateToTransaction(item.id) },
                                    onLongClick = {
                                        if (item.transactionType == "DEBT" || item.transactionType == "RECEIVABLE") {
                                            selectedTransactionToAction = item
                                        }
                                    },
                                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                            is TransferItemUI -> {
                                TransferItemRow(
                                    item = item,
                                    primaryBlue = primaryBlue,
                                    onClick = { onNavigateToTransfer(item.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    item(key = "spacer_${group.dateLabel}_$index") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        if (showFullImageDialog && selectedImagePath != null) {
            Dialog(
                onDismissRequest = { showFullImageDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showFullImageDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(selectedImagePath!!),
                        contentDescription = "Foto Transaksi Fullscreen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { showFullImageDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusChip(
    isSyncing: Boolean,
    isUserLoggedIn: Boolean,
    primaryBlue: Color,
    onClick: () -> Unit
) {
    val bgColor   = if (isSyncing)
        MaterialTheme.colorScheme.primaryContainer
    else if (!isUserLoggedIn)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isSyncing)
        MaterialTheme.colorScheme.onPrimaryContainer
    else if (!isUserLoggedIn)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isSyncing) {
            CircularProgressIndicator(
                modifier  = Modifier.size(14.dp),
                color     = textColor,
                strokeWidth = 1.5.dp
            )
        } else if (!isUserLoggedIn) {
            Icon(
                imageVector        = Icons.Outlined.ErrorOutline,
                contentDescription = "Belum Login",
                tint               = textColor,
                modifier           = Modifier.size(14.dp)
            )
        } else {
            Icon(
                imageVector        = Icons.Outlined.Sync,
                contentDescription = "Sinkron",
                tint               = textColor,
                modifier           = Modifier.size(14.dp)
            )
        }
        Text(
            text  = if (isSyncing) "Sinkronisasi..." else if (!isUserLoggedIn) "Belum Login" else "Sudah Sinkron",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color      = textColor,
                fontSize   = 11.sp
            )
        )
    }
}

@Composable
private fun DateRangeRow(
    label: String,
    onToggle: () -> Unit,
    primaryBlue: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width  = 1.dp,
                color  = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape  = RoundedCornerShape(10.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.CalendarMonth,
            contentDescription = "Rentang Tanggal",
            tint               = primaryBlue,
            modifier           = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Rentang Waktu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector        = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Buka Pilihan",
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FilterTabRow(
    selected: TransactionFilter,
    onSelect: (TransactionFilter) -> Unit,
    primaryBlue: Color,
    segmentedBg: Color,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(segmentedBg)
            .padding(4.dp)
    ) {
        val tabWidth = (maxWidth - 8.dp) / 3
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                TransactionFilter.SEMUA to "Semua",
                TransactionFilter.PENGELUARAN to "Pengeluaran",
                TransactionFilter.PEMASUKAN to "Pemasukan",
                TransactionFilter.TRANSFER to "Transfer",
                TransactionFilter.HUTANG to "Hutang",
                TransactionFilter.PIUTANG to "Piutang"
            ).forEach { (filter, label) ->
                FilterTab(
                    label       = label,
                    isSelected  = selected == filter,
                    onClick     = { onSelect(filter) },
                    primaryBlue = primaryBlue,
                    modifier    = Modifier.width(tabWidth)
                )
            }
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryBlue: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) primaryBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = if (isSelected) Color.White
                             else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRow(
    item: TransactionItemUI,
    incomeGreen: Color,
    expenseRed: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountColor = when (item.transactionType) {
        "EXPENSE", "DEBT" -> expenseRed
        "INCOME", "RECEIVABLE" -> incomeGreen
        else -> if (item.isExpense) expenseRed else incomeGreen
    }
    val iconColor = CategoryIconMapper.getColorByHex(item.colorHex)
    val bgColor = CategoryIconMapper.getBackgroundColorByHex(item.colorHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = CategoryIconMapper.getIconByName(item.iconName),
                contentDescription = item.category,
                tint               = iconColor,
                modifier           = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = item.title,
                style    = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = if (item.status == "LUNAS") "${item.time} • Lunas" else "${item.time} • ${item.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // if (item.receiptLocalPath != null) {
        //     Box(
        //         modifier = Modifier
        //             .padding(end = 8.dp)
        //             .size(36.dp)
        //             .clip(RoundedCornerShape(8.dp))
        //             .background(MaterialTheme.colorScheme.surfaceVariant)
        //             .clickable { onImageClick(item.receiptLocalPath) },
        //         contentAlignment = Alignment.Center
        //     ) {
        //         AsyncImage(
        //             model = File(item.receiptLocalPath),
        //             contentDescription = "Bukti Transaksi",
        //             contentScale = ContentScale.Crop,
        //             modifier = Modifier.fillMaxSize()
        //         )
        //     }
        // }

        Text(
            text  = item.amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = amountColor
            )
        )
    }
}



@Composable
fun TransferItemRow(
    item: TransferItemUI,
    primaryBlue: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(primaryBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.CompareArrows,
                contentDescription = null,
                tint               = primaryBlue,
                modifier           = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = "${item.fromAccountName} → ${item.toAccountName}",
                style    = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = "${item.time}${if (!item.notes.isNullOrEmpty()) " • ${item.notes}" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text  = item.amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFF57C00)
            )
        )
    }
}
