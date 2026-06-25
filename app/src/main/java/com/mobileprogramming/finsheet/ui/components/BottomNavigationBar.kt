package com.mobileprogramming.finsheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    onFabClick: () -> Unit = {},
    onBerandaClick: () -> Unit = {},
    onTransaksiClick: () -> Unit = {},
    selectedItem: String = "Beranda"     // kontrol tab aktif dari pemanggil
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        val selectedColor   = MaterialTheme.colorScheme.primary
        val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

        // Beranda
        NavigationBarItem(
            selected = selectedItem == "Beranda",
            onClick  = onBerandaClick,
            icon     = {
                Icon(
                    if (selectedItem == "Beranda") Icons.Filled.Home
                    else Icons.Outlined.Home,
                    contentDescription = "Beranda"
                )
            },
            label  = { Text("Beranda") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = selectedColor,
                unselectedIconColor = unselectedColor,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        )

        // Transaksi
        NavigationBarItem(
            selected = selectedItem == "Transaksi",
            onClick  = onTransaksiClick,
            icon     = {
                Icon(
                    if (selectedItem == "Transaksi") Icons.AutoMirrored.Filled.List
                    else Icons.AutoMirrored.Outlined.List,
                    contentDescription = "Transaksi"
                )
            },
            label  = { Text("Transaksi") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = selectedColor,
                unselectedIconColor = unselectedColor,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        )

        // Tambah (FAB center)
        NavigationBarItem(
            selected = false,
            onClick  = onFabClick,
            icon     = {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                }
            },
            label  = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = selectedColor,
                unselectedIconColor = unselectedColor,
                indicatorColor      = Color.Transparent
            )
        )

        // Anggaran
        NavigationBarItem(
            selected = selectedItem == "Anggaran",
            onClick  = { /*TODO*/ },
            icon     = {
                Icon(
                    if (selectedItem == "Anggaran") Icons.Filled.PieChart
                    else Icons.Outlined.PieChart,
                    contentDescription = "Anggaran"
                )
            },
            label  = { Text("Anggaran") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = selectedColor,
                unselectedIconColor = unselectedColor,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        )

        // Settings
        NavigationBarItem(
            selected = selectedItem == "Settings",
            onClick  = { /*TODO*/ },
            icon     = {
                Icon(
                    if (selectedItem == "Settings") Icons.Filled.Settings
                    else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label  = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor   = selectedColor,
                unselectedIconColor = unselectedColor,
                indicatorColor      = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        )
    }
}
