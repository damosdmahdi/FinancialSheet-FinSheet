package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Data models (UI-only — lift to domain model + ViewModel later)
// ---------------------------------------------------------------------------

private data class IconOption(
    val iconName: String,
    val icon: ImageVector
)

private val availableIcons = listOf(
    // Kategori khusus dari user
    IconOption("WaterDrop", Icons.Outlined.WaterDrop),
    IconOption("Bolt", Icons.Outlined.Bolt),
    IconOption("Build", Icons.Outlined.Build),
    IconOption("Savings", Icons.Outlined.Savings),
    IconOption("LocalGasStation", Icons.Outlined.LocalGasStation),
    IconOption("Shield", Icons.Outlined.Shield),
    IconOption("Bed", Icons.Outlined.Bed),
    IconOption("Wifi", Icons.Outlined.Wifi),
    IconOption("DirectionsBus", Icons.Outlined.DirectionsBus),
    
    // Kategori umum bawaan & populer
    IconOption("Restaurant", Icons.Outlined.Restaurant),
    IconOption("DirectionsCar", Icons.Outlined.DirectionsCar),
    IconOption("MenuBook", Icons.AutoMirrored.Outlined.MenuBook),
    IconOption("ShoppingCart", Icons.Outlined.ShoppingCart),
    IconOption("HealthAndSafety", Icons.Outlined.HealthAndSafety),
    IconOption("SportsEsports", Icons.Outlined.SportsEsports),
    IconOption("Home", Icons.Outlined.Home),
    IconOption("FlightTakeoff", Icons.Outlined.FlightTakeoff),
    IconOption("School", Icons.Outlined.School),
    IconOption("AccountBalanceWallet", Icons.Outlined.AccountBalanceWallet),
    IconOption("Laptop", Icons.Outlined.Laptop),
    IconOption("CardGiftcard", Icons.Outlined.CardGiftcard),
    IconOption("Storefront", Icons.Outlined.Storefront)
)

/** 7 warna sesuai desain: baris 1 = 5 warna, baris 2 = 2 warna */
private val availableColors = listOf(
    "1A3DA8",  // Navy Blue   (default terpilih)
    "2DC653",  // Hijau
    "FF8C00",  // Oranye
    "E53935",  // Merah
    "8E24AA",  // Ungu
    "E91E8C",  // Hot Pink
    "00ACC1"   // Teal
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    viewModel: AddCategoryViewModel,
    onNavigateBack: () -> Unit,
    onCategorySaved: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            state.createdCategoryId?.let { onCategorySaved(it) }
            onNavigateBack()
        }
    }

    val primaryBlue = Color(0xFF1A5BEB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Ubah Kategori" else "Tambah Kategori",
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
        bottomBar = {
            AddCategoryBottomBar(
                onCancel = onNavigateBack,
                onSave   = { viewModel.saveCategory() },
                primaryBlue = primaryBlue,
                isEditMode = state.isEditMode
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ----------------------------------------------------------------
            // 1. Nama Kategori
            // ----------------------------------------------------------------
            CategoryNameSection(
                value         = state.categoryName,
                onValueChange = { viewModel.onNameChanged(it) },
                primaryBlue   = primaryBlue
            )

            // ----------------------------------------------------------------
            // 2. Pilih Ikon
            // ----------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Pilih Ikon")
                IconPickerGrid(
                    icons          = availableIcons,
                    selectedIconName = state.selectedIcon,
                    selectedColorHex = state.selectedColorHex,
                    onIconSelected = { viewModel.onIconSelected(it) }
                )
            }

            // ----------------------------------------------------------------
            // 3. Pilih Warna
            // ----------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Pilih Warna")
                ColorPickerGrid(
                    colors          = availableColors,
                    selectedColorHex = state.selectedColorHex,
                    onColorSelected = { viewModel.onColorSelected(it) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Section title
// ---------------------------------------------------------------------------

@Composable
private fun SectionTitle(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ---------------------------------------------------------------------------
// 1. Nama Kategori
// ---------------------------------------------------------------------------

@Composable
private fun CategoryNameSection(
    value: String,
    onValueChange: (String) -> Unit,
    primaryBlue: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text  = "Nama Kategori",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    text  = "Cth: Kebutuhan Kost",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            },
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            singleLine = true,
            colors    = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = primaryBlue,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor   = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 2. Icon Picker Grid — manual Column+Row (4 kolom, tidak konflik scroll)
// ---------------------------------------------------------------------------

@Composable
private fun IconPickerGrid(
    icons: List<IconOption>,
    selectedIconName: String,
    selectedColorHex: String,
    onIconSelected: (String) -> Unit
) {
    val chunked = icons.chunked(4)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { option ->
                    Box(modifier = Modifier.weight(1f)) {
                        IconGridItem(
                            option        = option,
                            isSelected    = option.iconName == selectedIconName,
                            selectedColorHex = selectedColorHex,
                            onClick       = { onIconSelected(option.iconName) }
                        )
                    }
                }
                // Isi slot kosong di baris terakhir
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IconGridItem(
    option: IconOption,
    isSelected: Boolean,
    selectedColorHex: String,
    onClick: () -> Unit
) {
    val selectedColor = CategoryIconMapper.getColorByHex(selectedColorHex)
    val borderColor = if (isSelected) selectedColor
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor     = if (isSelected) selectedColor.copy(alpha = 0.08f)
                      else MaterialTheme.colorScheme.surface
    val iconTint    = if (isSelected) selectedColor
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = option.icon,
            contentDescription = null,
            modifier           = Modifier.size(28.dp),
            tint               = iconTint
        )
    }
}

// ---------------------------------------------------------------------------
// 3. Color Picker Grid — baris pertama 5, baris kedua 2
// ---------------------------------------------------------------------------

@Composable
private fun ColorPickerGrid(
    colors: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val chunked = colors.chunked(5)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowColors.forEach { colorHex ->
                    ColorSwatch(
                        colorHex   = colorHex,
                        isSelected = colorHex == selectedColorHex,
                        onClick    = { onColorSelected(colorHex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = CategoryIconMapper.getColorByHex(colorHex)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector        = Icons.Outlined.Check,
                contentDescription = "Dipilih",
                tint               = Color.White,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Action Bar
// ---------------------------------------------------------------------------

@Composable
private fun AddCategoryBottomBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    primaryBlue: Color,
    isEditMode: Boolean
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Batal
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.5.dp, primaryBlue),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryBlue
                )
            ) {
                Text(
                    text  = "Batal",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Simpan Kategori
            Button(
                onClick  = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text  = if (isEditMode) "Simpan Perubahan" else "Simpan Kategori",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
