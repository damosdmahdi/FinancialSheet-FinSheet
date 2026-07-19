package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Data model (UI-only — replace with domain model + ViewModel)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Helper mapping
// ---------------------------------------------------------------------------
private data class SelectableCategoryItem(
    val entity: com.mobileprogramming.finsheet.data.local.entity.CategoryEntity,
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCategoryScreen(
    viewModel: AddEditTransactionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    /* ---- Local UI state ---- */
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryEntity by remember(state.selectedCategory) {
        mutableStateOf(state.selectedCategory)
    }
    var categoryToDelete by remember { mutableStateOf<SelectableCategoryItem?>(null) }

    val primaryBlue = Color(0xFF1A5BEB)

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Hapus Kategori") },
            text = { Text("Apakah Anda yakin ingin menghapus kategori \"${categoryToDelete?.label}\"? Semua alokasi anggaran dan riwayat transaksi untuk kategori ini akan terpengaruh.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it.entity.id) }
                        categoryToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Filter categories by search query
    val allCategories = remember(state.categories) {
        state.categories
            .filter { it.categoryName != "Lainnya" }
            .map {
                SelectableCategoryItem(
                    entity = it,
                    label = it.categoryName,
                    icon = CategoryIconMapper.getIconByName(it.icon),
                    tint = CategoryIconMapper.getColorByHex(it.color)
                )
            }
    }
    
    val filteredCategories = remember(searchQuery, allCategories) {
        val base = if (searchQuery.isBlank()) allCategories
        else allCategories.filter {
            it.label.contains(searchQuery, ignoreCase = true)
        }
        val mutable = base.toMutableList()
        if (searchQuery.isBlank() || "tambah".contains(searchQuery, ignoreCase = true)) {
            mutable.add(
                SelectableCategoryItem(
                    entity = com.mobileprogramming.finsheet.data.local.entity.CategoryEntity(
                        id = "virtual-add-category",
                        categoryName = "Tambah Kategori",
                        type = "EXPENSE",
                        icon = "Add",
                        color = "7B7FA6"
                    ),
                    label = "Tambah",
                    icon = Icons.Default.Add,
                    tint = Color(0xFF7B7FA6)
                )
            )
        }
        mutable
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Kategori",
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
        containerColor = MaterialTheme.colorScheme.background,
        // Pinned bottom button via bottomBar slot
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { 
                        selectedCategoryEntity?.let {
                            viewModel.onCategorySelected(it)
                            onNavigateBack()
                        }
                    },
                    enabled = selectedCategoryEntity != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White,
                        disabledContainerColor = primaryBlue.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Konfirmasi Kategori",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ----------------------------------------------------------------
            // Search Bar  (full-width span)
            // ----------------------------------------------------------------
            item(span = { GridItemSpan(4) }) {
                Spacer(modifier = Modifier.height(4.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    primaryBlue = primaryBlue
                )
            }

            // ----------------------------------------------------------------
            // Info Banner  (full-width span)
            // ----------------------------------------------------------------
            item(span = { GridItemSpan(4) }) {
                InfoBannerCard(primaryBlue = primaryBlue)
            }

            // ----------------------------------------------------------------
            // Section header  (full-width span)
            // ----------------------------------------------------------------
            item(span = { GridItemSpan(4) }) {
                Text(
                    text = "KATEGORI TERPOPULER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            if (allCategories.isEmpty()) {
                item(span = { GridItemSpan(4) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Kategori belanja belum tersedia. Silakan tambahkan kategori terlebih dahulu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // ----------------------------------------------------------------
            // Category grid items
            // ----------------------------------------------------------------
            items(filteredCategories) { item ->
                SelectableCategoryGridItem(
                    item = item,
                    isSelected = selectedCategoryEntity?.id == item.entity.id,
                    primaryBlue = primaryBlue,
                    onEdit = { onNavigateToEditCategory(item.entity.id) },
                    onDelete = { categoryToDelete = item },
                    onClick = {
                        if (item.entity.id == "virtual-add-category") {
                            onNavigateToAddCategory()
                        } else {
                            selectedCategoryEntity = item.entity
                        }
                    }
                )
            }

            // ----------------------------------------------------------------
            // Bottom spacing so last row isn't hidden behind the button
            // ----------------------------------------------------------------
            item(span = { GridItemSpan(4) }) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Search Bar
// ---------------------------------------------------------------------------

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    primaryBlue: Color
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Cari kategori belanja...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ---------------------------------------------------------------------------
// Info Banner Card
// ---------------------------------------------------------------------------

@Composable
private fun InfoBannerCard(primaryBlue: Color) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            primaryBlue,
            Color(0xFF3A7BFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // Decorative faint circle on the right
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Column {
            Text(
                text = "Organisir Keuanganmu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pilih kategori yang paling sesuai\nuntuk melacak pengeluaran\nmahasiswa kamu dengan akurat.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Category Grid Item
// ---------------------------------------------------------------------------

@Composable
private fun SelectableCategoryGridItem(
    item: SelectableCategoryItem,
    isSelected: Boolean,
    primaryBlue: Color,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val solidColor = CategoryIconMapper.getColorByHex(item.entity.color)
    val circleBgColor = CategoryIconMapper.getBackgroundColorByHex(item.entity.color)
    
    val borderColor = if (isSelected) primaryBlue
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val cardBgColor = if (isSelected) primaryBlue.copy(alpha = 0.08f)
    else MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.aspectRatio(0.85f)) {
        OutlinedCard(
            onClick = onClick,
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = cardBgColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(circleBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = solidColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        if (item.entity.id != "virtual-add-category" && item.entity.categoryName != "Lainnya") {
            if (onEdit != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(28.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
                        contentDescription = "Edit Kategori",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                        contentDescription = "Hapus Kategori",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
