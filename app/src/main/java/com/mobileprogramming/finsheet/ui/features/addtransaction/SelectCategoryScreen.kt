package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    var selectedCategoryEntity by remember { mutableStateOf<com.mobileprogramming.finsheet.data.local.entity.CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<com.mobileprogramming.finsheet.data.local.entity.CategoryEntity?>(null) }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Hapus Kategori") },
            text = { Text("Apakah Anda yakin ingin menghapus kategori \"${categoryToDelete?.categoryName}\"? Semua alokasi anggaran dan riwayat transaksi untuk kategori ini akan terpengaruh.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it.id) }
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

    val primaryBlue = Color(0xFF1A5BEB)

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
        if (searchQuery.isBlank()) allCategories
        else allCategories.filter {
            it.label.contains(searchQuery, ignoreCase = true)
        }
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

            // ----------------------------------------------------------------
            // Category grid items
            // ----------------------------------------------------------------
            items(filteredCategories) { item ->
                SelectableCategoryGridItem(
                    item = item,
                    isSelected = selectedCategoryEntity?.id == item.entity.id,
                    primaryBlue = primaryBlue,
                    onEdit = { onNavigateToEditCategory(item.entity.id) },
                    onDelete = { categoryToDelete = item.entity },
                    onClick = { selectedCategoryEntity = item.entity }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) primaryBlue
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val bgColor = if (isSelected) primaryBlue.copy(alpha = 0.08f)
    else MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.aspectRatio(1f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) primaryBlue else item.tint
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) primaryBlue
                    else MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.entity.categoryName != "Lainnya") {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(30.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Kategori",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus Kategori",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
