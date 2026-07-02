package com.mobileprogramming.finsheet.ui.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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

    val primaryBlue   = Color(0xFF1A5BEB)
    val incomeGreen   = Color(0xFF2DC653)
    val expenseRed    = Color(0xFFE53935)
    val segmentedBg   = MaterialTheme.colorScheme.surfaceContainerHigh

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
                    val isUserLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
                    SyncStatusChip(
                        isSyncing = isSyncing,
                        isUserLoggedIn = isUserLoggedIn,
                        primaryBlue = primaryBlue,
                        onClick   = {
                            if (!isSyncing) {
                                isSyncing = true
                                val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                                if (email != null) {
                                    viewModel.syncToGoogleSheets(email) { success ->
                                        isSyncing = false
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Berhasil Sinkron!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Gagal Sinkron atau Tidak ada data baru", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    isSyncing = false
                                    android.widget.Toast.makeText(context, "Harap login dengan Google", android.widget.Toast.LENGTH_SHORT).show()
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
            } else if (uiState.transactions.isEmpty()) {
                item(key = "empty_state") {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada transaksi.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                uiState.transactions.forEachIndexed { index, group ->
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
                    ) { tx ->
                        TransactionRow(
                            item        = tx,
                            incomeGreen = incomeGreen,
                            expenseRed  = expenseRed,
                            onClick     = { onNavigateToTransaction(tx.id) },
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item(key = "spacer_${group.dateLabel}_$index") {
                        Spacer(modifier = Modifier.height(8.dp))
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(segmentedBg)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterTab(
                label       = "Semua",
                isSelected  = selected == TransactionFilter.SEMUA,
                onClick     = { onSelect(TransactionFilter.SEMUA) },
                primaryBlue = primaryBlue,
                modifier    = Modifier.weight(1f)
            )
            FilterTab(
                label       = "Pengeluaran",
                isSelected  = selected == TransactionFilter.PENGELUARAN,
                onClick     = { onSelect(TransactionFilter.PENGELUARAN) },
                primaryBlue = primaryBlue,
                modifier    = Modifier.weight(1f)
            )
            FilterTab(
                label       = "Pemasukan",
                isSelected  = selected == TransactionFilter.PEMASUKAN,
                onClick     = { onSelect(TransactionFilter.PEMASUKAN) },
                primaryBlue = primaryBlue,
                modifier    = Modifier.weight(1f)
            )
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

@Composable
private fun TransactionRow(
    item: TransactionItemUI,
    incomeGreen: Color,
    expenseRed: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountColor = if (item.isExpense) expenseRed else incomeGreen
    val iconColor = CategoryIconMapper.getColorByHex(item.colorHex)
    val bgColor = CategoryIconMapper.getBackgroundColorByHex(item.colorHex)

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
                text  = "${item.time} • ${item.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text  = item.amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = amountColor
            )
        )
    }
}
