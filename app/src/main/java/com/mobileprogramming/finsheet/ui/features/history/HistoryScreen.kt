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
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {}
) {
    /* ---- Local UI state (lift to ViewModel later) ---- */
    var selectedFilter       by remember { mutableStateOf(TransactionFilter.SEMUA) }
    var dateRangeText        by remember { mutableStateOf("1 Okt - 10 Okt 2023") }
    var dateDropdownExpanded by remember { mutableStateOf(false) }

    val primaryBlue   = Color(0xFF1A5BEB)
    val incomeGreen   = Color(0xFF2DC653)
    val expenseRed    = Color(0xFFE53935)
    val segmentedBg   = Color(0xFFE8ECF5)
    val syncGreenBg   = Color(0xFFE8F5E9)
    val syncGreenText = Color(0xFF2E7D32)

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
                    containerColor          = MaterialTheme.colorScheme.surface,
                    titleContentColor       = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        // BottomNavigationBar di-pin di sini sehingga LazyColumn tidak mendorongnya
        bottomBar = {
            BottomNavigationBar(
                onFabClick       = onNavigateToAddTransaction,
                onTransaksiClick = { /* sudah berada di layar ini */ },
                selectedItem     = "Transaksi"   // tab aktif
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
            // Header row: "Riwayat" title + "Sudah Sinkron" chip
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
                    SyncStatusChip(
                        bgColor   = syncGreenBg,
                        textColor = syncGreenText
                    )
                }
            }

            // ----------------------------------------------------------------
            // Date range selector
            // ----------------------------------------------------------------
            item {
                DateRangeRow(
                    label       = dateRangeText,
                    expanded    = dateDropdownExpanded,
                    onToggle    = { dateDropdownExpanded = !dateDropdownExpanded },
                    onDismiss   = { dateDropdownExpanded = false },
                    primaryBlue = primaryBlue,
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ----------------------------------------------------------------
            // Filter tabs: Semua / Pengeluaran / Pemasukan
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
                    TransactionRow(
                        item        = tx,
                        primaryBlue = primaryBlue,
                        incomeGreen = incomeGreen,
                        expenseRed  = expenseRed,
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

/** Chip "Sudah Sinkron" dengan ikon centang hijau */
@Composable
private fun SyncStatusChip(bgColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.CheckCircle,
            contentDescription = "Sinkron",
            tint               = textColor,
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text  = "Sudah Sinkron",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color      = textColor,
                fontSize   = 11.sp
            )
        )
    }
}

/** Baris pemilih rentang tanggal */
@Composable
private fun DateRangeRow(
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    primaryBlue: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
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

        DropdownMenu(
            expanded          = expanded,
            onDismissRequest  = onDismiss
        ) {
            listOf(
                "1 Okt - 10 Okt 2023",
                "11 Okt - 20 Okt 2023",
                "21 Okt - 31 Okt 2023"
            ).forEach { range ->
                DropdownMenuItem(
                    text    = { Text(range) },
                    onClick = { onDismiss() }
                )
            }
        }
    }
}

/** Tab filter Semua / Pengeluaran / Pemasukan */
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
                modifier    = Modifier.weight(1.4f)
            )
            FilterTab(
                label       = "Pemasukan",
                isSelected  = selected == TransactionFilter.PEMASUKAN,
                onClick     = { onSelect(TransactionFilter.PEMASUKAN) },
                primaryBlue = primaryBlue,
                modifier    = Modifier.weight(1.2f)
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

/** Baris satu transaksi */
@Composable
private fun TransactionRow(
    item: TransactionItem,
    primaryBlue: Color,
    incomeGreen: Color,
    expenseRed: Color,
    modifier: Modifier = Modifier
) {
    val amountColor = if (item.isExpense) expenseRed else incomeGreen

    Row(
        modifier = modifier.fillMaxWidth(),
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
