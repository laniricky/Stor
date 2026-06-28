package com.stor.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.stor.domain.model.Expense
import com.stor.domain.model.Loan
import com.stor.presentation.navigation.Screen
import com.stor.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(TealPrimary.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Good morning 👋", fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            Text("Your Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                        IconButton(onClick = { onNavigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TealPrimary)
                        }
                    }
                }
            }
        }

        item {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(TealPrimary, TealPrimary.copy(alpha = 0.7f)))
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text("Total Balance", fontSize = 14.sp, color = BackgroundDark.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatKsh(state.dashboard?.totalBalance ?: 0.0),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            MiniStat("Income", formatKsh(state.dashboard?.monthlyIncome ?: 0.0), IncomeColor)
                            MiniStat("Spent", formatKsh(state.dashboard?.monthlySpending ?: 0.0), ExpenseColor)
                            MiniStat("Debt", formatKsh(state.dashboard?.outstandingDebt ?: 0.0), LoanColor)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            // Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAction("Add Expense", Icons.Default.Remove, ExpenseColor, Modifier.weight(1f)) {
                    onNavigate(Screen.AddExpense.route)
                }
                QuickAction("Add Income", Icons.Default.Add, IncomeColor, Modifier.weight(1f)) {
                    onNavigate(Screen.AddIncome.route)
                }
                QuickAction("Add Loan", Icons.Default.AccountBalance, LoanColor, Modifier.weight(1f)) {
                    onNavigate(Screen.AddLoan.route)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Active Loans
        if (!state.dashboard?.upcomingLoanPayments.isNullOrEmpty()) {
            item {
                SectionHeader("Active Loans", "View all") { onNavigate(Screen.Loans.route) }
            }
            items(state.dashboard!!.upcomingLoanPayments.take(3)) { loan ->
                LoanProgressCard(loan = loan, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Recent Transactions
        item {
            SectionHeader("Recent Transactions", "View all") { onNavigate(Screen.Expenses.route) }
        }

        if (state.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
        } else if (!state.dashboard?.recentExpenses.isNullOrEmpty()) {
            items(state.dashboard!!.recentExpenses) { expense ->
                TransactionItem(expense = expense, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = BackgroundDark.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BackgroundDark)
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                        color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text(action, fontSize = 13.sp, color = TealPrimary,
            modifier = Modifier.clickable { onAction() })
    }
}

@Composable
private fun LoanProgressCard(loan: Loan, modifier: Modifier = Modifier) {
    val progress = (loan.percentagePaid / 100).coerceIn(0.0, 1.0).toFloat()
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(loan.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(formatKsh(loan.remainingBalance), fontWeight = FontWeight.SemiBold, color = LoanColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(loan.lender, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = LoanColor, trackColor = CardBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${String.format("%.0f", loan.percentagePaid)}% paid",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun TransactionItem(expense: Expense, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(ExpenseColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null,
                    tint = ExpenseColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(expense.category, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-${formatKsh(expense.amount)}", fontWeight = FontWeight.SemiBold, color = ExpenseColor)
                Text(expense.date, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

fun formatKsh(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    nf.maximumFractionDigits = 0
    return "KSh ${nf.format(amount)}"
}
