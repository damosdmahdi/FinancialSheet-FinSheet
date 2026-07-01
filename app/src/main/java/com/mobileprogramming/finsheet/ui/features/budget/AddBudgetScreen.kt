package com.mobileprogramming.finsheet.ui.features.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprogramming.finsheet.core.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconColorLight: Color,
    val bgColorLight: Color,
    val iconColorDark: Color,
    val bgColorDark: Color
)

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        
        val formattedText = try {
            val number = originalText.toLong()
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.Builder().setLanguage("id").setRegion("ID").build()))
            formatter.format(number).replace(',', '.')
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                var digitsCount = 0
                for (i in 0 until formattedText.length) {
                    if (digitsCount == offset) return i
                    if (formattedText[i].isDigit()) {
                        digitsCount++
                    }
                }
                return formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formattedText.isEmpty()) return 0
                var digitsCount = 0
                for (i in 0 until minOf(offset, formattedText.length)) {
                    if (formattedText[i].isDigit()) {
                        digitsCount++
                    }
                }
                return digitsCount
            }
        }
        
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    viewModel: AddBudgetViewModel = viewModel(
        factory = com.mobileprogramming.finsheet.di.Injection.provideAddBudgetViewModelFactory(
            LocalContext.current.applicationContext
        )
    ),
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var amountState by remember { mutableStateOf(TextFieldValue("")) }
    var showAllCategories by remember { mutableStateOf(false) }

    val dbCategories = remember(uiState.categories) {
        uiState.categories
            .filter { it.categoryName != "Lainnya" }
            .map { entity ->
                val icon = CategoryIconMapper.getIconByName(entity.icon)
                val solidColor = CategoryIconMapper.getColorByHex(entity.color)
                val bgColor = CategoryIconMapper.getBackgroundColorByHex(entity.color)
                CategoryItem(
                    id = entity.id,
                    name = entity.categoryName,
                    icon = icon,
                    iconColorLight = solidColor,
                    bgColorLight = bgColor,
                    iconColorDark = solidColor,
                    bgColorDark = bgColor
                )
            }
    }

    val budgetGridCategories = remember(dbCategories) {
        val list = dbCategories.take(7).toMutableList()
        list.add(
            CategoryItem(
                id = "virtual-add-category",
                name = "Tambah",
                icon = Icons.Filled.Add,
                iconColorLight = Color(0xFF7B7FA6),
                bgColorLight = Color(0xFFF0F0F8),
                iconColorDark = Color(0xFF7B7FA6),
                bgColorDark = Color(0xFFF0F0F8)
            )
        )
        list
    }

    if (showAllCategories) {
        CategorySelectionScreen(
            categories = dbCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onConfirm = { showAllCategories = false },
            onBack = { showAllCategories = false }
        )
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Alokasi Kategori",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val category = selectedCategory
                        val amount = amountState.text.toLongOrNull()
                        if (category != null && amount != null) {
                            viewModel.saveBudget(
                                categoryId = category.id,
                                budgetName = "Batas Anggaran ${category.name}",
                                amountLimit = amount,
                                onComplete = onNavigateBack
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = selectedCategory != null && amountState.text.isNotBlank() && amountState.text != "0"
                ) {
                    Text("Simpan Alokasi", style = MaterialTheme.typography.labelLarge)
                }
                
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Batal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Categories (Top 8 for main screen)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showAllCategories = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dbCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val chunkedCategories = budgetGridCategories.chunked(4)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        chunkedCategories.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { category ->
                                    CategoryCard(
                                        category = category,
                                        isSelected = selectedCategory?.id == category.id,
                                        onClick = {
                                            if (category.id == "virtual-add-category") {
                                                onNavigateToAddCategory()
                                            } else {
                                                selectedCategory = category
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size < 4) {
                                    repeat(4 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Nominal Anggaran
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nominal Anggaran",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = amountState,
                        onValueChange = { newValue ->
                            val filtered = newValue.text.filter { it.isDigit() }
                            if (filtered.length <= 15) {
                                amountState = newValue.copy(text = filtered)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = {
                            Text(
                                text = "Rp",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        },
                        visualTransformation = RupiahVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Masukkan batas anggaran untuk kategori ini",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val iconColor = if (isDark) category.iconColorDark else category.iconColorLight
    val bgColor = if (isDark) category.bgColorDark else category.bgColorLight

    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.aspectRatio(0.85f)
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
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    categories: List<CategoryItem>,
    selectedCategory: CategoryItem?,
    onCategorySelected: (CategoryItem) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pilih Kategori",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Konfirmasi Kategori", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari kategori belanja...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Organisir Keuanganmu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih kategori yang paling sesuai untuk melacak pengeluaran mahasiswa kamu dengan akurat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "KATEGORI TERPOPULER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            val filteredCategories = categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
            val chunkedCategories = filteredCategories.chunked(4)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                chunkedCategories.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { category ->
                            CategoryCard(
                                category = category,
                                isSelected = selectedCategory?.id == category.id,
                                onClick = { onCategorySelected(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < 4) {
                            repeat(4 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}