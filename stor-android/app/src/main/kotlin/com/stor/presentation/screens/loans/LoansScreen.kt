package com.stor.presentation.screens.loans

import androidx.compose.foundation.*
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
import androidx.navigation.NavController
import com.stor.domain.model.Loan
import com.stor.presentation.navigation.Screen
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.*

@Composable
fun LoansScreen(
    navController: NavController,
    viewModel: LoansViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val totalDebt = state.loans.filter { loan -> loan.status == "active" }.sumOf { loan -> loan.remainingBalance }

    LaunchedEffect(Unit) {
        viewModel.sync()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddLoan.route) },
                containerColor = LoanColor, contentColor = BackgroundDark, shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Loan") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(LoanColor.copy(alpha = 0.12f), Color.Transparent)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Loans", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text("Outstanding debt", fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = { viewModel.sync() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LoanColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(formatKsh(totalDebt), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LoanColor)
                        Text(
                            "across ${state.loans.count { loan -> loan.status == "active" }} active loans",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LoanColor)
                    }
                }
            } else if (state.loans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No loans yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                }
            } else {
                item {
                    Text("Your Loans", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground)
                }
                items(items = state.loans, key = { loan -> loan.id }) { loan ->
                    LoanCard(
                        loan = loan,
                        onViewRepayments = { navController.navigate(Screen.Repayments.createRoute(loan.id)) },
                        onDelete = { viewModel.delete(loan.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanCard(loan: Loan, onViewRepayments: () -> Unit, onDelete: () -> Unit) {
    val progress = (loan.percentagePaid / 100).coerceIn(0.0, 1.0).toFloat()
    val statusColor = if (loan.status == "active") LoanColor else IncomeColor
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(loan.name, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(loan.lender, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        loan.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(formatKsh(loan.remainingBalance), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LoanColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Original", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(formatKsh(loan.originalAmount), fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = LoanColor, trackColor = LoanColor.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${String.format("%.1f", loan.percentagePaid)}% paid",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(formatKsh(loan.amountPaid) + " paid",
                    fontSize = 11.sp, color = LoanColor.copy(alpha = 0.7f))
            }

            if (loan.monthlyPayment != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (loan.interestRate != null) {
                        LoanChip("${loan.interestRate}% interest")
                    }
                    LoanChip("${formatKsh(loan.monthlyPayment)}/mo")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewRepayments,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, LoanColor)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null,
                        modifier = Modifier.size(16.dp), tint = LoanColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Repayments", color = LoanColor, fontSize = 13.sp)
                }
                IconButton(
                    onClick = { showConfirm = true },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(ExpenseColor.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = ExpenseColor, modifier = Modifier.size(18.dp))
                }
            }

            if (showConfirm) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showConfirm = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    TextButton(onClick = { onDelete(); showConfirm = false }) {
                        Text("Delete", color = ExpenseColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanChip(text: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = LoanColor.copy(alpha = 0.1f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp, color = LoanColor)
    }
}
