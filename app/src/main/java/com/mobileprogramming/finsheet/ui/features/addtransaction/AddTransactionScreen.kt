package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit
) {
    var transactionName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Makanan") }
    var transactionType by remember { mutableStateOf("Pengeluaran") }
    
    val categories = listOf("Makanan", "Transportasi", "Pendidikan", "Hiburan", "Lainnya")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Transaction Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = transactionType == "Pengeluaran",
                    onClick = { transactionType = "Pengeluaran" },
                    label = { Text("Pengeluaran") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                FilterChip(
                    selected = transactionType == "Pemasukan",
                    onClick = { transactionType = "Pemasukan" },
                    label = { Text("Pemasukan") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            // Input Fields
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Jumlah (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                prefix = { Text("Rp") }
            )

            OutlinedTextField(
                value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Nama Transaksi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            
            // Date Picker Placeholder
            OutlinedTextField(
                value = "12 Juni 2026",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Simpan Transaksi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
