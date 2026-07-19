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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileprogramming.finsheet.ui.components.BottomNavigationBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import com.mobileprogramming.finsheet.domain.usecase.budget.GetBudgetScreenDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.DeleteBudgetUseCase
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper

// State and ViewModels are imported from BudgetViewModel.kt

fun formatRupiah(amount: String): String {
    if (amount.isEmpty()) return "0"
    return try {
        val number = amount.toLong()
        val formatter = java.text.DecimalFormat("#,###", java.text.DecimalFormatSymbols(java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()))
        formatter.format(number).replace(',', '.')
    } catch (e: Exception) {
        amount
    }
}

// --- UI Components ---

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(
        factory = com.mobileprogramming.finsheet.di.Injection.provideBudgetViewModelFactory(
            LocalContext.current.applicationContext
        )
    ),
    onNavigateToBeranda: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddBudget: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var allocationToDelete by remember { mutableStateOf<BudgetCategoryState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (allocationToDelete != null) {
        AlertDialog(
            onDismissRequest = { allocationToDelete = null },
            title = { Text("Hapus Alokasi Anggaran") },
            text = { Text("Apakah Anda yakin ingin menghapus alokasi anggaran untuk kategori \"${allocationToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        allocationToDelete?.let { viewModel.deleteCategory(it.id) }
                        allocationToDelete = null
                        Toast.makeText(context, "Alokasi anggaran berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { allocationToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

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
                                                text = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.getSymbol(uiState.selectedCurrency),
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
                                    OutlinedTextField(
                                        value = uiState.totalBudget,
                                        onValueChange = {},
                                        enabled = false,
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        leadingIcon = {
                                            Text(
                                                text = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.getSymbol(uiState.selectedCurrency),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        singleLine = true,
                                        visualTransformation = RupiahVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.primary,
                                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val unallocatedVal = uiState.unallocatedBudget.toLongOrNull() ?: 0L
                            val isExceeded = unallocatedVal < 0L
                            val containerColor = if (isExceeded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                            val contentColor = if (isExceeded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            val infoText = if (isExceeded) {
                                "Melebihi total anggaran sebesar ${com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format((-unallocatedVal).toString(), uiState.selectedCurrency)}"
                            } else {
                                "Tersisa ${com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(unallocatedVal.toString(), uiState.selectedCurrency)} belum dialokasikan"
                            }
                            
                            Surface(
                                color = containerColor,
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
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = infoText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor
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
                        if (!uiState.isEditing) {
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
                    currencyCode = uiState.selectedCurrency,
                    onAmountChanged = { newAmount ->
                        viewModel.updateCategoryAmount(category.id, newAmount)
                    },
                    onDelete = {
                        allocationToDelete = category
                    }
                )
            }

            item {
                AnimatedVisibility(visible = uiState.isEditing) {
                    val unallocatedVal = uiState.unallocatedBudget.toLongOrNull() ?: 0L
                    val isExceeded = unallocatedVal < 0L
                    Button(
                        onClick = {
                            if (!isExceeded) {
                                viewModel.saveChanges()
                                Toast.makeText(context, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isExceeded,
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
    currencyCode: String,
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
                    val iconColor = CategoryIconMapper.getColorByHex(category.colorHex)
                    val bgColor = CategoryIconMapper.getBackgroundColorByHex(category.colorHex)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = bgColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = iconColor
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
            
            if (isEditing) {
                OutlinedTextField(
                    value = category.allocatedAmount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    leadingIcon = {
                        Text(
                            text = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.getSymbol(currencyCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    visualTransformation = RupiahVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                )
            } else {
                OutlinedTextField(
                    value = category.allocatedAmount,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    leadingIcon = {
                        Text(
                            text = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.getSymbol(currencyCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    visualTransformation = RupiahVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.primary,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val dailyText = if (category.dailyAmount.isNotEmpty() && category.dailyAmount != "0") {
                "perhari sekitar ${com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(category.dailyAmount, currencyCode)}"
            } else null
            
            val weeklyText = if (category.weeklyAmount.isNotEmpty() && category.weeklyAmount != "0") {
                "perminggu sekitar ${com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(category.weeklyAmount, currencyCode)}"
            } else null

            if (dailyText != null || weeklyText != null) {
                val combinedText = listOfNotNull(dailyText, weeklyText).joinToString(" | ")
                Text(
                    text = "Anggaran $combinedText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (category.reallocationLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                category.reallocationLabels.forEach { label ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (label.startsWith("Telah")) Color(0xFFE53935) else Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (label.startsWith("Telah")) Color(0xFFE53935) else Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}