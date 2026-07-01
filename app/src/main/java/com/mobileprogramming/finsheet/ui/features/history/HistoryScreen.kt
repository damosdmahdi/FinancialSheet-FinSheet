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

// ---------------------------------------------------------------------------
// Data models (UI-only — lift to domain + ViewModel later)
// ---------------------------------------------------------------------------

private enum class TransactionFilter { SEMUA, PENGELUARAN, PEMASUKAN }

private data class TransactionItem(
    val title: String,
    val time: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean,
    val icon: ImageVector
)

private data class TransactionGroup(
    val dateLabel: String,
    val items: List<TransactionItem>
)

/** Data dummy sesuai desain */
private val mockTransactionGroups = listOf(
    TransactionGroup(
        dateLabel = "Hari ini",
        items = listOf(
            TransactionItem(
                title     = "Makan Siang Kopma",
                time      = "12:30",
                category  = "Makanan",
                amount    = "-Rp 35.000",
                isExpense = true,
                icon      = Icons.Outlined.Restaurant
            ),
            TransactionItem(
                title     = "Transfer dari Orang Tus",
                time      = "09:15",
                category  = "Pemasukan",
                amount    = "+Rp 1.500.000",
                isExpense = false,
                icon      = Icons.Outlined.AccountBalance
            )
        )
    ),
    TransactionGroup(
        dateLabel = "Kemarin",
        items = listOf(
            TransactionItem(
                title     = "Isi Bensin Motor",
                time      = "16:45",
                category  = "Transportasi",
                amount    = "-Rp 25.000",
                isExpense = true,
                icon      = Icons.Outlined.DirectionsCar
            ),
            TransactionItem(
                title     = "Buku Catatan Kuliah",
                time      = "10:20",
                category  = "Edukasi",
                amount    = "-Rp 45.000",
                isExpense = true,
                icon      = Icons.AutoMirrored.Outlined.MenuBook
            )
        )
    ),
    TransactionGroup(
        dateLabel = "10 Okt 2023",
        items = listOf(
            TransactionItem(
                title     = "Belanja Bulanan Kos",
                time      = "19:00",
                category  = "Kebutuhan",
                amount    = "-Rp 150.000",
                isExpense = true,
                icon      = Icons.Outlined.ShoppingCart
            )
        )
    )
)

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatDateMillis(millis: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    return sdf.format(Date(millis))
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTransaction: () -> Unit = {},   // [REVISI 3] navigasi ke halaman transaksi
    onNavigateToAnggaran: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    /* ---- Local UI state ---- */
    var selectedFilter          by remember { mutableStateOf(TransactionFilter.SEMUA) }
    var showDatePicker          by remember { mutableStateOf(false) }          // [REVISI 1]
    var isSyncing               by remember { mutableStateOf(false) }         // [REVISI 2]

    // [REVISI 1] Rentang tanggal dari DateRangePicker
    val dateRangePickerState = rememberDateRangePickerState()
    val dateRangeText = remember(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
    ) {
        val start = dateRangePickerState.selectedStartDateMillis
        val end   = dateRangePickerState.selectedEndDateMillis
        when {
            start != null && end != null ->
                "${formatDateMillis(start)} - ${formatDateMillis(end)}"
            start != null ->
                "${formatDateMillis(start)} - ..."
            else ->
                "1 Okt - 10 Okt 2023"
        }
    }

    val primaryBlue   = Color(0xFF1A5BEB)
    val incomeGreen   = Color(0xFF2DC653)
    val expenseRed    = Color(0xFFE53935)
    val segmentedBg   = MaterialTheme.colorScheme.surfaceContainerHigh

    // Filter the groups based on selected tab
    val filteredGroups = remember(selectedFilter) {
        when (selectedFilter) {
            TransactionFilter.SEMUA -> mockTransactionGroups
            TransactionFilter.PENGELUARAN -> mockTransactionGroups.mapNotNull { group ->
                val filtered = group.items.filter { it.isExpense }
                if (filtered.isEmpty()) null else group.copy(items = filtered)
            }
            TransactionFilter.PEMASUKAN -> mockTransactionGroups.mapNotNull { group ->
                val filtered = group.items.filter { !it.isExpense }
                if (filtered.isEmpty()) null else group.copy(items = filtered)
            }
        }
    }

    // [REVISI 1] Modal DateRangePicker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
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
            // ----------------------------------------------------------------
            // Header row: "Riwayat" title + Sync chip
            // ----------------------------------------------------------------
            item {
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
                    // [REVISI 2] Chip yang bisa diklik untuk sinkron manual
                    SyncStatusChip(
                        isSyncing = isSyncing,
                        primaryBlue = primaryBlue,
                        onClick   = {
                            if (!isSyncing) {
                                isSyncing = true
                                // TODO: panggil ViewModel.sync() di sini
                                // Simulasi selesai sinkron setelah 2 detik
                            }
                        }
                    )
                }
            }

            // ----------------------------------------------------------------
            // [REVISI 1] Date range selector — membuka modal DateRangePicker
            // ----------------------------------------------------------------
            item {
                DateRangeRow(
                    label       = dateRangeText,
                    onToggle    = { showDatePicker = true },
                    primaryBlue = primaryBlue,
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ----------------------------------------------------------------
            // [REVISI 4] Filter tabs: Semua / Pengeluaran / Pemasukan — ukuran sama
            // ----------------------------------------------------------------
            item {
                FilterTabRow(
                    selected    = selectedFilter,
                    onSelect    = { selectedFilter = it },
                    primaryBlue = primaryBlue,
                    segmentedBg = segmentedBg,
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ----------------------------------------------------------------
            // Transaction groups
            // ----------------------------------------------------------------
            filteredGroups.forEach { group ->
                // Date section header
                item(key = "header_${group.dateLabel}") {
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

                // Transaction items in the group
                items(
                    items = group.items,
                    key   = { "${group.dateLabel}_${it.title}_${it.time}" }
                ) { tx ->
                    // [REVISI 3] Klik item → navigasi ke halaman transaksi
                    TransactionRow(
                        item        = tx,
                        primaryBlue = primaryBlue,
                        incomeGreen = incomeGreen,
                        expenseRed  = expenseRed,
                        onClick     = onNavigateToTransaction,
                        modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Spacing after each group
                item(key = "spacer_${group.dateLabel}") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

/**
 * [REVISI 2] Chip "Sudah Sinkron" / "Sinkronkan" yang bisa diklik untuk sinkron manual.
 */
@Composable
private fun SyncStatusChip(
    isSyncing: Boolean,
    primaryBlue: Color,
    onClick: () -> Unit
) {
    val bgColor   = if (isSyncing)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isSyncing)
        MaterialTheme.colorScheme.onPrimaryContainer
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
        } else {
            Icon(
                imageVector        = Icons.Outlined.CheckCircle,
                contentDescription = "Sinkron",
                tint               = textColor,
                modifier           = Modifier.size(14.dp)
            )
        }
        Text(
            text  = if (isSyncing) "Sinkronisasi..." else "Sudah Sinkron",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color      = textColor,
                fontSize   = 11.sp
            )
        )
    }
}

/**
 * [REVISI 1] Baris pemilih rentang tanggal — onToggle membuka DateRangePicker modal.
 */
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

/**
 * [REVISI 4] Tab filter Semua / Pengeluaran / Pemasukan dengan ukuran yang sama rata.
 */
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
            // weight(1f) yang sama untuk ketiga tombol
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

/**
 * [REVISI 3] Baris satu transaksi — klik membuka halaman edit transaksi.
 */
@Composable
private fun TransactionRow(
    item: TransactionItem,
    primaryBlue: Color,
    incomeGreen: Color,
    expenseRed: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountColor = if (item.isExpense) expenseRed else incomeGreen

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)         // [REVISI 3] navigasi ke transaksi
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Circular icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(primaryBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.category,
                tint               = primaryBlue,
                modifier           = Modifier.size(22.dp)
            )
        }

        // Title + time • category
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

        // Amount
        Text(
            text  = item.amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = amountColor
            )
        )
    }
}
