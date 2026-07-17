package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconMapper {
    fun getIconByName(name: String?): ImageVector {
        return when (name) {
            "Restaurant" -> Icons.Outlined.Restaurant
            "DirectionsCar" -> Icons.Outlined.DirectionsCar
            "MenuBook" -> Icons.AutoMirrored.Outlined.MenuBook
            "ShoppingCart" -> Icons.Outlined.ShoppingCart
            "HealthAndSafety" -> Icons.Outlined.HealthAndSafety
            "SportsEsports" -> Icons.Outlined.SportsEsports
            "Home" -> Icons.Outlined.Home
            "FlightTakeoff" -> Icons.Outlined.FlightTakeoff
            "School" -> Icons.Outlined.School
            "MoreHoriz" -> Icons.Outlined.MoreHoriz
            "Savings" -> Icons.Outlined.Savings
            "AccountBalanceWallet" -> Icons.Outlined.AccountBalanceWallet
            "Laptop" -> Icons.Outlined.Laptop
            "CardGiftcard" -> Icons.Outlined.CardGiftcard
            "Storefront" -> Icons.Outlined.Storefront
            "Add" -> Icons.Filled.Add
            "WaterDrop" -> Icons.Outlined.WaterDrop
            "Bolt" -> Icons.Outlined.Bolt
            "Build" -> Icons.Outlined.Build
            "LocalGasStation" -> Icons.Outlined.LocalGasStation
            "Shield" -> Icons.Outlined.Shield
            "Bed" -> Icons.Outlined.Bed
            "Wifi" -> Icons.Outlined.Wifi
            "DirectionsBus" -> Icons.Outlined.DirectionsBus
            else -> Icons.Outlined.MoreHoriz
        }
    }

    fun getColorByHex(hex: String?): Color {
        if (hex == null) return Color(0xFF7B7FA6) // Default fallback color
        return try {
            Color(android.graphics.Color.parseColor("#$hex"))
        } catch (e: Exception) {
            Color(0xFF7B7FA6)
        }
    }

    // A helper to derive a light background color from the solid icon color
    fun getBackgroundColorByHex(hex: String?): Color {
        val solidColor = getColorByHex(hex)
        // Simple trick: make it highly transparent for background
        return solidColor.copy(alpha = 0.15f)
    }
}
