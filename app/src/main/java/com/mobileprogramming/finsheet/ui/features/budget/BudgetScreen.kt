package com.mobileprogramming.finsheet.ui.features.budget

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileprogramming.finsheet.ui.components.BottomNavigationBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- State and ViewModels ---

data class BudgetCategoryState(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val allocatedAmount: String,
    val dailyAmount: String
)

data class BudgetUiState(
    val totalBudget: String = "3500000",
    val unallocatedBudget: String = "500000",
    val isEditing: Boolean = false,
    val categories: List<BudgetCategoryState> = listOf(
        BudgetCategoryState("1", "Makanan", Icons.Filled.Restaurant, "1500000", "50000"),
        BudgetCategoryState("2", "Transportasi", Icons.Filled.DirectionsBus, "150000", "5000"),
        BudgetCategoryState("3", "Buku & Kuliah", Icons.AutoMirrored.Filled.MenuBook, "500000", "16000"),
        BudgetCategoryState("4", "Hiburan", Icons.Filled.LocalPlay, "0", "0")
    )
)

class BudgetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun updateTotalBudget(newAmount: String) {
        val filtered = newAmount.filter { it.isDigit() }
        if (filtered.length <= 15) {
            _uiState.update { it.copy(totalBudget = filtered) }
        }
    }

    fun updateCategoryAmount(id: String, newAmount: String) {
        val filtered = newAmount.filter { it.isDigit() }
        if (filtered.length <= 15) {
            _uiState.update { state ->
                val updatedCategories = state.categories.map { category ->
                    if (category.id == id) {
                        category.copy(allocatedAmount = filtered)
                    } else {
                        category
                    }
                }
                state.copy(categories = updatedCategories)
            }
        }
    }

    fun deleteCategory(id: String) {
        _uiState.update { state ->
            state.copy(categories = state.categories.filter { it.id != id })
        }
    }

    fun saveChanges() {
        _uiState.update { it.copy(isEditing = false) }
    }
}

fun formatRupiah(amount: String): String {
    if (amount.isEmpty()) return "0"
    return try {
        val number = amount.toLong()
        val formatter = java.text.DecimalFormat("#,###", java.text.DecimalFormatSymbols(java.util.Locale("id", "ID")))
        formatter.format(number).replace(',', '.')
    } catch (e: Exception) {
        amount
    }
}

// --- UI Components ---

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    onNavigateToBeranda: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddBudget: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Anggaran",
                onBerandaClick = onNavigateToBeranda,
                onTransaksiClick = onNavigateToTransaksi,
                onFabClick = onNavigateToAddTransaction,
                onSettingsClick = onNavigateToSettings
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header Section
            item {
                Column {
                    Text(
                        text = "Pengaturan Anggaran",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kelola batas pengeluaranmu agar keuangan tetap aman.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Total Budget Card
            item {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Total Anggaran Bulanan",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (uiState.isEditing) {
                                    OutlinedTextField(
                                        value = uiState.totalBudget,
                                        onValueChange = { viewModel.updateTotalBudget(it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        leadingIcon = {
                                            Text(
                                                text = "Rp",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        singleLine = true,
                                        visualTransformation = RupiahVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        ),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = "Rp ${formatRupiah(uiState.totalBudget)}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tersisa Rp ${formatRupiah(uiState.unallocatedBudget)} belum dialokasikan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Categories Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alokasi Kategori",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (!uiState.isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onNavigateToAddBudget,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Tambah",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Tambah")
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleEditMode() },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Ubah",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Ubah")
                            }
                        }
                    }
                }
            }

            // Category List
            items(uiState.categories) { category ->
                BudgetCategoryItem(
                    category = category,
                    isEditing = uiState.isEditing,
                    onAmountChanged = { newAmount ->
                        viewModel.updateCategoryAmount(category.id, newAmount)
                    },
                    onDelete = {
                        viewModel.deleteCategory(category.id)
                    }
                )
            }
            
            item {
                // Save Button (only visible when editing)
                AnimatedVisibility(visible = uiState.isEditing) {
                    Button(
                        onClick = {
                            viewModel.saveChanges()
                            Toast.makeText(context, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(text = "Simpan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCategoryItem(
    category: BudgetCategoryState,
    isEditing: Boolean,
    onAmountChanged: (String) -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isEditing) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus Kategori",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Batas Anggaran",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = category.allocatedAmount,
                onValueChange = onAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                leadingIcon = {
                    Text(
                        text = "Rp",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isEditing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                visualTransformation = RupiahVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (category.dailyAmount.isNotEmpty()) {
                Text(
                    text = "Anggaran perhari sekitar Rp ${formatRupiah(category.dailyAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}