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
            "WaterDrop" -> Icons.Outlined.WaterDrop
            "Bolt" -> Icons.Outlined.Bolt
            "Build" -> Icons.Outlined.Build
            "LocalGasStation" -> Icons.Outlined.LocalGasStation
            "Shield" -> Icons.Outlined.Shield
            "Bed" -> Icons.Outlined.Bed
            "Wifi" -> Icons.Outlined.Wifi
            "DirectionsBus" -> Icons.Outlined.DirectionsBus
            "Add" -> Icons.Filled.Add
            "CreditCard" -> Icons.Outlined.CreditCard
            "Payments" -> Icons.Outlined.Payments
            "MonetizationOn" -> Icons.Outlined.MonetizationOn
            "LocalAtm" -> Icons.Outlined.LocalAtm
            "AccountBalance" -> Icons.Outlined.AccountBalance
            "TrendingUp" -> Icons.Outlined.TrendingUp
            "PriceChange" -> Icons.Outlined.PriceChange
            "AttachMoney" -> Icons.Outlined.AttachMoney
            "Paid" -> Icons.Outlined.Paid
            "DEBT" -> Icons.Outlined.TrendingDown
            "RECEIVABLE" -> Icons.Outlined.TrendingUp
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

    private val colorOrder = listOf(
        "1A3DA8", // Navy Blue
        "2DC653", // Hijau
        "FF8C00", // Oranye
        "E53935", // Merah
        "8E24AA", // Ungu
        "E91E8C", // Hot Pink
        "00ACC1"  // Teal
    )

    fun sortCategoriesByColor(categories: List<com.mobileprogramming.finsheet.data.local.entity.CategoryEntity>): List<com.mobileprogramming.finsheet.data.local.entity.CategoryEntity> {
        return categories.sortedWith(
            compareBy<com.mobileprogramming.finsheet.data.local.entity.CategoryEntity> { category ->
                val hex = category.color?.uppercase()?.removePrefix("#") ?: ""
                val index = colorOrder.indexOf(hex)
                if (index != -1) index else colorOrder.size
            }.thenBy { it.categoryName }
        )
    }
}
