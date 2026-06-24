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

private data class SelectableCategoryItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color = Color(0xFF1A5BEB)          // default blue; override per-item below
)

private val popularCategories = listOf(
    SelectableCategoryItem("Makanan",   Icons.Outlined.Restaurant,    Color(0xFFE65100)),
    SelectableCategoryItem("Transport", Icons.Outlined.DirectionsCar, Color(0xFF1565C0)),
    SelectableCategoryItem("Edukasi",   Icons.Outlined.School,        Color(0xFF6A1B9A)),
    SelectableCategoryItem("Belanja",   Icons.Outlined.ShoppingCart,  Color(0xFFAD1457)),
    SelectableCategoryItem("Hiburan",   Icons.Outlined.SportsEsports, Color(0xFF00695C)),
    SelectableCategoryItem("Simpanan",  Icons.Outlined.Savings,       Color(0xFF2E7D32)),
    SelectableCategoryItem("Kesehatan", Icons.Outlined.HealthAndSafety, Color(0xFFC62828)),
    SelectableCategoryItem("Kuota",     Icons.Outlined.Wifi,          Color(0xFF0277BD)),
    SelectableCategoryItem("Kos",       Icons.Outlined.Home,          Color(0xFF4527A0)),
    SelectableCategoryItem("Lainnya",   Icons.Outlined.MoreHoriz,     Color(0xFF37474F))
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCategoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit
) {
    /* ---- Local UI state (lift to ViewModel later) ---- */
    var searchQuery by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf<String?>(null) }

    val primaryBlue = Color(0xFF1A5BEB)

    // Filter categories by search query
    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) popularCategories
        else popularCategories.filter {
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
                    onClick = { /* TODO: dispatch confirmed category to ViewModel */ },
                    enabled = selectedLabel != null,
                    modifier = Modifier
                        .fillMaxWidth()
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
                    isSelected = selectedLabel == item.label,
                    primaryBlue = primaryBlue,
                    onClick = { selectedLabel = item.label }
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
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) primaryBlue
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val bgColor = if (isSelected) primaryBlue.copy(alpha = 0.08f)
    else MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .aspectRatio(1f)
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
}
