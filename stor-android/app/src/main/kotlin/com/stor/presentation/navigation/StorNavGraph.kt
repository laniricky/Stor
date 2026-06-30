package com.stor.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stor.presentation.screens.auth.LoginScreen
import com.stor.presentation.screens.auth.RegisterScreen
import com.stor.presentation.screens.dashboard.DashboardScreen
import com.stor.presentation.screens.expenses.ExpensesScreen
import com.stor.presentation.screens.expenses.AddExpenseScreen
import com.stor.presentation.screens.income.IncomeScreen
import com.stor.presentation.screens.income.AddIncomeScreen
import com.stor.presentation.screens.loans.LoansScreen
import com.stor.presentation.screens.loans.AddLoanScreen
import com.stor.presentation.screens.loans.RepaymentsScreen
import com.stor.presentation.screens.loans.AddRepaymentScreen
import com.stor.presentation.screens.more.MoreScreen
import com.stor.presentation.theme.CardBackground
import com.stor.presentation.theme.TealPrimary

private val authRoutes = setOf(Screen.Login.route, Screen.Register.route)

@Composable
fun StorNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in authRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = CardBackground,
                    tonalElevation = 0.dp
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(onNavigate = { navController.navigate(it) })
                }
                composable(Screen.Expenses.route) {
                    ExpensesScreen(navController = navController)
                }
                composable(Screen.Income.route) {
                    IncomeScreen(navController = navController)
                }
                composable(Screen.Loans.route) {
                    LoansScreen(navController = navController)
                }
                composable(Screen.More.route) {
                    MoreScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onLogoutComplete = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.AddExpense.route) {
                    AddExpenseScreen(navController = navController)
                }
                composable(Screen.AddIncome.route) {
                    AddIncomeScreen(navController = navController)
                }
                composable(Screen.AddLoan.route) {
                    AddLoanScreen(navController = navController)
                }
                composable(Screen.Repayments.route) { backStackEntry ->
                    val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
                    RepaymentsScreen(navController = navController, loanId = loanId)
                }
                composable(Screen.AddRepayment.route) { backStackEntry ->
                    val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
                    AddRepaymentScreen(navController = navController, loanId = loanId)
                }
                composable(Screen.EditProfile.route) {
                    com.stor.presentation.screens.more.profile.EditProfileScreen(navController = navController)
                }
                composable(Screen.ChangePassword.route) {
                    com.stor.presentation.screens.more.profile.ChangePasswordScreen(navController = navController)
                }
                composable(Screen.BudgetSettings.route) {
                    com.stor.presentation.screens.more.budget.BudgetSettingsScreen(navController = navController)
                }
                composable(Screen.NotificationSettings.route) {
                    com.stor.presentation.screens.more.notifications.NotificationSettingsScreen(navController = navController)
                }
                composable(Screen.ExportReports.route) {
                    com.stor.presentation.screens.more.export.ExportReportsScreen(navController = navController)
                }
            }
        }
    }
}
