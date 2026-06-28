package com.stor.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Expenses, "Expenses", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavItem(Screen.Income, "Income", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
    BottomNavItem(Screen.Loans, "Loans", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    BottomNavItem(Screen.More, "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz),
)
