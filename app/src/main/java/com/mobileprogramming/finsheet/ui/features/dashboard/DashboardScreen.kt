package com.mobileprogramming.finsheet.ui.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileprogramming.finsheet.core.theme.tertiaryFixedDimLight
import com.mobileprogramming.finsheet.ui.components.BottomNavigationBar
import com.mobileprogramming.finsheet.ui.components.BudgetProgressItem
import com.mobileprogramming.finsheet.ui.components.CategoryExpenseItem
import com.mobileprogramming.finsheet.ui.components.FilterChipsRow

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(onFabClick = onNavigateToAddTransaction) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Balance Card
            TopBalanceCard(
                totalBalance = uiState.totalBalance,
                income = uiState.incomeThisMonth,
                expense = uiState.expenseThisMonth
            )

            // Add Transaction Button
            Button(
                onClick = onNavigateToAddTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tambah Transaksi",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Pengeluaran Kategori Card
            ExpenseCategoryCard(
                selectedFilterIndex = uiState.selectedFilterIndex,
                onFilterSelected = viewModel::setFilterIndex,
                totalExpenseForFilter = uiState.totalExpenseForFilter,
                categories = uiState.categoryExpenses
            )

            // Anggaran Bulan Ini
            MonthlyBudgetSection(
                budgets = uiState.monthlyBudgets
            )
        }
    }
}

@Composable
fun TopBalanceCard(
    totalBalance: String,
    income: String,
    expense: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Total Saldo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = totalBalance,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = "Pemasukan",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pemasukan bulan ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = income,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Pengeluaran",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pengeluaran bulan ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseCategoryCard(
    selectedFilterIndex: Int,
    onFilterSelected: (Int) -> Unit,
    totalExpenseForFilter: String,
    categories: List<CategoryExpenseData>
) {
    val filters = listOf("Hari Ini", "Minggu Ini", "Bulan Ini")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Pengeluaran Kategori",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilterChipsRow(
                options = filters,
                selectedIndex = selectedFilterIndex,
                onOptionSelected = onFilterSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Total Pengeluaran",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = totalExpenseForFilter,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            categories.forEach { category ->
                CategoryExpenseItem(
                    color = getCategoryColor(category.categoryType),
                    categoryName = category.categoryName,
                    percentage = category.percentage
                )
            }
        }
    }
}

@Composable
fun MonthlyBudgetSection(budgets: List<BudgetProgressData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Anggaran Bulan Ini",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { /*TODO*/ }) {
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        budgets.forEach { budget ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val color = getCategoryColor(budget.categoryType)
                    val iconColorParams = getCategoryIconColors(budget.categoryType)

                    BudgetProgressItem(
                        icon = getCategoryIcon(budget.categoryType),
                        iconBackgroundColor = iconColorParams.first,
                        iconTintColor = iconColorParams.second,
                        budgetName = budget.budgetName,
                        percentage = budget.percentage,
                        progress = budget.progress,
                        usedAmountStr = budget.usedAmountStr,
                        totalAmountStr = budget.totalAmountStr,
                        remainingAmountStr = budget.remainingAmountStr,
                        progressColor = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun getCategoryColor(type: ExpenseCategoryType): Color {
    return when (type) {
        ExpenseCategoryType.FOOD -> MaterialTheme.colorScheme.primary
        ExpenseCategoryType.TRANSPORTATION -> MaterialTheme.colorScheme.secondaryContainer
        ExpenseCategoryType.EDUCATION -> tertiaryFixedDimLight
        ExpenseCategoryType.OTHERS -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun getCategoryIconColors(type: ExpenseCategoryType): Pair<Color, Color> {
    return when (type) {
        ExpenseCategoryType.FOOD -> Pair(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.primary)
        ExpenseCategoryType.TRANSPORTATION -> Pair(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.secondaryContainer)
        ExpenseCategoryType.EDUCATION -> Pair(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f), MaterialTheme.colorScheme.tertiaryContainer)
        ExpenseCategoryType.OTHERS -> Pair(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun getCategoryIcon(type: ExpenseCategoryType): ImageVector {
    return when (type) {
        ExpenseCategoryType.FOOD -> Icons.Filled.Restaurant
        ExpenseCategoryType.TRANSPORTATION -> Icons.Filled.DirectionsTransit
        ExpenseCategoryType.EDUCATION -> Icons.Filled.Book
        ExpenseCategoryType.OTHERS -> Icons.Filled.Category
    }
}
