package com.stor.presentation.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stor.domain.model.ChartDataPoint
import com.stor.domain.model.RecentTransaction
import com.stor.domain.model.UpcomingPayment
import com.stor.presentation.navigation.Screen
import com.stor.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

// Dummy chart data is used as fallback when API returns empty list
private val fallbackChartData = listOf(
    ChartDataPoint("Jan", 0.0, 0.0),
    ChartDataPoint("Feb", 0.0, 0.0),
    ChartDataPoint("Mar", 0.0, 0.0),
    ChartDataPoint("Apr", 0.0, 0.0),
    ChartDataPoint("May", 0.0, 0.0),
    ChartDataPoint("Jun", 0.0, 0.0)
)

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── TOP BAR ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    "Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // ── TOTAL BALANCE ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Total Balance",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (state.isLoading) "Loading..." else formatKsh(state.dashboard?.totalBalance ?: 0.0),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Updated just now",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TealPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── 2x2 SUMMARY CARDS ────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Income (This Month)",
                        value = formatKsh(state.dashboard?.monthlyIncome ?: 0.0),
                        color = IncomeColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Expenses (This Month)",
                        value = formatKsh(state.dashboard?.monthlySpending ?: 0.0),
                        color = ExpenseColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Outstanding Loans",
                        value = formatKsh(state.dashboard?.outstandingDebt ?: 0.0),
                        color = LoanColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Today's Spending",
                        value = formatKsh(state.dashboard?.todaySpending ?: 0.0),
                        color = Color(0xFF9C6FFF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // ── QUICK ACTIONS ─────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionButton(
                        label = "Expense",
                        icon = Icons.Default.Remove,
                        color = ExpenseColor,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.AddExpense.route) }
                    QuickActionButton(
                        label = "Income",
                        icon = Icons.Default.Add,
                        color = IncomeColor,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.AddIncome.route) }
                    QuickActionButton(
                        label = "Loan",
                        icon = Icons.Default.AccountBalance,
                        color = LoanColor,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate(Screen.AddLoan.route) }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // ── OVERVIEW BAR CHART ───────────────────────────────────────
        item {
            val chartData = state.dashboard?.monthlyChart
                ?.takeIf { it.isNotEmpty() } ?: fallbackChartData
            OverviewChartCard(data = chartData, isRealData = !state.dashboard?.monthlyChart.isNullOrEmpty())
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // ── UPCOMING LOAN PAYMENTS ───────────────────────────────────
        if (!state.dashboard?.upcomingLoanPayments.isNullOrEmpty()) {
            item {
                SectionHeader("Upcoming Loan Payments", "View all") { onNavigate(Screen.Loans.route) }
            }
            items(state.dashboard!!.upcomingLoanPayments.take(2)) { payment ->
                UpcomingLoanItem(
                    payment = payment,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // ── RECENT TRANSACTIONS ───────────────────────────────────────
        item {
            SectionHeader("Recent Transactions", "View all") { onNavigate(Screen.Expenses.route) }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
        } else if (!state.dashboard?.recentTransactions.isNullOrEmpty()) {
            items(state.dashboard!!.recentTransactions) { tx ->
                RecentTransactionItem(
                    transaction = tx,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        } else if (state.error != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Could not load data",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.load() }) {
                            Text("Retry", color = TealPrimary)
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No transactions yet",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ── COMPOSABLES ───────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Add $label",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun OverviewChartCard(data: List<ChartDataPoint>, isRealData: Boolean = false) {
    val incomeColor = IncomeColor
    val expenseColor = ExpenseColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot("Income", incomeColor)
                    LegendDot("Expenses", expenseColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val maxVal = data.maxOf { maxOf(it.income, it.expenses) }
                    .coerceAtLeast(1.0).toFloat()
                val groupWidth = size.width / data.size
                val barWidth = groupWidth * 0.2f
                val gap = barWidth * 0.4f
                val maxBarHeight = size.height * 0.85f

                data.forEachIndexed { i, point ->
                    val groupLeft = i * groupWidth + groupWidth * 0.1f
                    val incomeH = (point.income.toFloat() / maxVal) * maxBarHeight
                    val expH = (point.expenses.toFloat() / maxVal) * maxBarHeight

                    // Income bar
                    drawRoundRect(
                        color = incomeColor,
                        topLeft = Offset(groupLeft, size.height - incomeH),
                        size = Size(barWidth, incomeH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    // Expense bar
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(groupLeft + barWidth + gap, size.height - expH),
                        size = Size(barWidth, expH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { point ->
                    Text(
                        point.label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }

            if (!isRealData) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Add transactions to see real trends",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        Text(
            action, fontSize = 13.sp, color = TealPrimary,
            modifier = Modifier.clickable { onAction() }
        )
    }
}

@Composable
private fun UpcomingLoanItem(payment: UpcomingPayment, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LoanColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = LoanColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.loanName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(
                    payment.dueLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            Text(
                formatKsh(payment.amount),
                fontWeight = FontWeight.Bold,
                color = ExpenseColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun RecentTransactionItem(transaction: RecentTransaction, modifier: Modifier = Modifier) {
    val icon = categoryIcon(transaction.category)
    val color = categoryColor(transaction.category)
    val isExpense = transaction.type == "expense"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    transaction.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val amountText = if (isExpense) "-${formatKsh(kotlin.math.abs(transaction.amount))}"
                                 else "+${formatKsh(transaction.amount)}"
                Text(
                    amountText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isExpense) ExpenseColor else IncomeColor
                )
                Text(
                    displayDate(transaction.date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ── HELPERS ───────────────────────────────────────────────────────────────────

private fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "food" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "rent" -> Icons.Default.Home
    "utilities" -> Icons.Default.ElectricBolt
    "shopping" -> Icons.Default.ShoppingBag
    "entertainment" -> Icons.Default.MusicNote
    "health" -> Icons.Default.LocalHospital
    "education" -> Icons.Default.School
    else -> Icons.Default.Receipt
}

private fun categoryColor(category: String): Color = when (category.lowercase()) {
    "food" -> Color(0xFFFF8C42)
    "transport" -> Color(0xFF3D9BE9)
    "rent" -> Color(0xFF9C6FFF)
    "utilities" -> Color(0xFFFFCA28)
    "shopping" -> Color(0xFFE91E8C)
    "entertainment" -> Color(0xFF2BCA7E)
    "health" -> Color(0xFFFF5252)
    "education" -> Color(0xFF4CAF50)
    else -> Color(0xFF9E9E9E)
}

private fun displayDate(dateStr: String): String {
    return try {
        val today = java.time.LocalDate.now()
        val date = java.time.LocalDate.parse(dateStr)
        when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

fun formatKsh(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    nf.maximumFractionDigits = 0
    return "KSH ${nf.format(amount)}"
}
