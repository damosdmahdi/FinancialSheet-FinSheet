package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Data model (UI-only, to be replaced by domain model + ViewModel later)
// ---------------------------------------------------------------------------

private enum class TransactionType { PENGELUARAN, PEMASUKAN }

private data class CategoryItem(
    val label: String,
    val icon: ImageVector
)

private val defaultCategories = listOf(
    CategoryItem("Makanan", Icons.Outlined.Restaurant),
    CategoryItem("Transport", Icons.Outlined.DirectionsCar),
    CategoryItem("Belanja", Icons.Outlined.ShoppingCart),
    CategoryItem("Edukasi", Icons.Outlined.School),
    CategoryItem("Lainnya", Icons.Outlined.MoreHoriz),
    CategoryItem("Tambah", Icons.Filled.Add)
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSelectCategory: () -> Unit,
    onNavigateToAddCategory: () -> Unit
) {
    /* ---- Local UI state (to be lifted to ViewModel) ---- */
    var selectedType by remember { mutableStateOf(TransactionType.PENGELUARAN) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var amountText by remember { mutableStateOf("15.50") }
    var selectedCategory by remember { mutableStateOf("Makanan") }
    var dateText by remember { mutableStateOf("10/27/2023") }
    var noteText by remember { mutableStateOf("") }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    val primaryBlue = Color(0xFF1A5BEB)
    val lightGrayBg = Color(0xFFF0F2F8)
    val segmentedBg = Color(0xFFE8ECF5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ----------------------------------------------------------------
            // 1. Segmented Pill Toggle
            // ----------------------------------------------------------------
            SegmentedTypeToggle(
                selected = selectedType,
                onSelect = { selectedType = it },
                primaryBlue = primaryBlue,
                segmentedBg = segmentedBg
            )

            // ----------------------------------------------------------------
            // 2. Amount Input Card
            // ----------------------------------------------------------------
            AmountInputCard(
                currency = selectedCurrency,
                amountText = amountText,
                onAmountChange = { amountText = it },
                dropdownExpanded = currencyDropdownExpanded,
                onDropdownToggle = { currencyDropdownExpanded = !currencyDropdownExpanded },
                onDropdownDismiss = { currencyDropdownExpanded = false },
                onCurrencySelected = {
                    selectedCurrency = it
                    currencyDropdownExpanded = false
                },
                lightGrayBg = lightGrayBg,
                primaryBlue = primaryBlue
            )

            // ----------------------------------------------------------------
            // 3. Kategori Label + Grid
            // ----------------------------------------------------------------
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            CategoryGrid(
                categories = defaultCategories,
                selectedLabel = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onNavigateToSelectCategory = onNavigateToSelectCategory,
                onNavigateToAddCategory = onNavigateToAddCategory,
                primaryBlue = primaryBlue
            )

            // ----------------------------------------------------------------
            // 4. Tanggal Field
            // ----------------------------------------------------------------
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Tanggal") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Pilih Tanggal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // ----------------------------------------------------------------
            // 5. Catatan Field
            // ----------------------------------------------------------------
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Catatan (Opsional)") },
                placeholder = { Text("Contoh: Beli buku referensi") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.List,
                        contentDescription = "Catatan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // ----------------------------------------------------------------
            // 6. Dokumentasi Gambar – Dashed Upload Box
            // ----------------------------------------------------------------
            Text(
                text = "Dokumentasi Gambar",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            DashedUploadBox(
                onClick = { /* TODO: open camera/gallery */ },
                primaryBlue = primaryBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ----------------------------------------------------------------
            // 7. Action Buttons Row
            // ----------------------------------------------------------------
            ActionButtonsRow(
                onCancel = onNavigateBack,
                onSave = { /* TODO: dispatch ViewModel save action */ },
                primaryBlue = primaryBlue
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun SegmentedTypeToggle(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
    primaryBlue: Color,
    segmentedBg: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(segmentedBg)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Pengeluaran tab
            val isExpense = selected == TransactionType.PENGELUARAN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isExpense) primaryBlue else Color.Transparent)
                    .clickable { onSelect(TransactionType.PENGELUARAN) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pengeluaran",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Pemasukan tab
            val isIncome = selected == TransactionType.PEMASUKAN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isIncome) primaryBlue else Color.Transparent)
                    .clickable { onSelect(TransactionType.PEMASUKAN) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pemasukan",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AmountInputCard(
    currency: String,
    amountText: String,
    onAmountChange: (String) -> Unit,
    dropdownExpanded: Boolean,
    onDropdownToggle: () -> Unit,
    onDropdownDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    lightGrayBg: Color,
    primaryBlue: Color
) {
    val currencies = listOf("USD", "IDR", "EUR", "SGD", "JPY")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = lightGrayBg,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Nominal Transaksi",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onDropdownToggle() }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Pilih Mata Uang",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = onDropdownDismiss
                    ) {
                        currencies.forEach { cur ->
                            DropdownMenuItem(
                                text = { Text(cur) },
                                onClick = { onCurrencySelected(cur) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Amount BasicTextField
                BasicTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(primaryBlue),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "≈ Rp241.800",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryItem>,
    selectedLabel: String,
    onCategorySelected: (String) -> Unit,
    onNavigateToSelectCategory: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    primaryBlue: Color
) {
    // Manual grid: Column of Rows — avoids LazyVerticalGrid height conflict
    // inside a scrollable parent Column.
    val chunked = categories.chunked(4)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    // Routing per-label: dua callback berbeda untuk dua aksi berbeda
                    val clickAction: () -> Unit = when (item.label) {
                        "Lainnya" -> onNavigateToSelectCategory   // pilih dari kategori yang sudah ada
                        "Tambah"  -> onNavigateToAddCategory       // buat kategori baru
                        else      -> { { onCategorySelected(item.label) } }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        CategoryGridItem(
                            item = item,
                            isSelected = item.label == selectedLabel,
                            onClick = clickAction,
                            primaryBlue = primaryBlue
                        )
                    }
                }
                // Fill remaining empty slots in the last row so items keep their width
                val emptySlots = 4 - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
private fun CategoryGridItem(
    item: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryBlue: Color
) {
    val containerColor = if (isSelected) primaryBlue else Color.Transparent
    val borderColor = if (isSelected) primaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            modifier = Modifier.size(26.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun DashedUploadBox(
    onClick: () -> Unit,
    primaryBlue: Color
) {
    // Compose doesn't have a built-in dashed border; approximate with a
    // solid low-alpha border and a dashed look via PathEffect via Canvas,
    // but for simplicity we use a border with rounded shape + a subtle bg.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF0F4FF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner dashed border via DrawScope
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(12f, 8f), 0f
                )
            )
            drawRoundRect(
                color = primaryBlue.copy(alpha = 0.5f),
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Upload Foto",
                tint = primaryBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ambil Gambar atau Unggah Foto",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = primaryBlue
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    primaryBlue: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Batal – Outlined Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, primaryBlue),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = primaryBlue
            )
        ) {
            Text(
                text = "Batal",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Simpan Transaksi – Solid Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(2f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Simpan Transaksi",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
