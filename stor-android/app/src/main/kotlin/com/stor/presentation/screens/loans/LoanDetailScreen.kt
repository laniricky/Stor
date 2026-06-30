package com.stor.presentation.screens.loans

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import com.stor.presentation.navigation.Screen
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    navController: NavController,
    loanId: String,
    viewModel: LoanDetailViewModel = hiltViewModel()
) {
    val loan by viewModel.loan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadLoan(loanId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        loan?.name ?: "Loan Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LoanColor)
                    }
                },
                actions = {
                    if (loan != null) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Repayments.createRoute(loan!!.id))
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Repayments", tint = LoanColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (loan != null) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddRepayment.createRoute(loan!!.id)) },
                    containerColor = LoanColor,
                    contentColor = BackgroundDark,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Repayment")
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LoanColor)
                }
            }
            loan == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Loan not found", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
            else -> {
                val currentLoan = loan!!
                val progress = (currentLoan.percentagePaid / 100).coerceIn(0.0, 1.0).toFloat()
                val statusColor = if (currentLoan.status == "active") LoanColor else IncomeColor

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero gradient header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(LoanColor.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                                Text(
                                    currentLoan.status.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    fontSize = 13.sp, color = statusColor, fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Remaining Balance",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                formatKsh(currentLoan.remainingBalance),
                                fontSize = 38.sp, fontWeight = FontWeight.Bold, color = LoanColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "from ${formatKsh(currentLoan.originalAmount)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = LoanColor,
                                trackColor = LoanColor.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "${String.format("%.1f", currentLoan.percentagePaid)}% paid",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                Text(
                                    formatKsh(currentLoan.amountPaid) + " paid",
                                    fontSize = 12.sp, color = LoanColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Details section
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoanStatCard(modifier = Modifier.weight(1f), label = "Lender", value = currentLoan.lender)
                            LoanStatCard(modifier = Modifier.weight(1f), label = "Start Date", value = currentLoan.startDate)
                        }

                        val hasInterest = currentLoan.interestRate != null
                        val hasMonthly = currentLoan.monthlyPayment != null
                        if (hasInterest || hasMonthly) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (hasInterest) {
                                    LoanStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Interest Rate",
                                        value = "${currentLoan.interestRate}%"
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                if (hasMonthly) {
                                    LoanStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Monthly Payment",
                                        value = formatKsh(currentLoan.monthlyPayment!!)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        val hasDueDay = currentLoan.dueDay != null
                        val hasEndDate = currentLoan.endDate != null
                        if (hasDueDay || hasEndDate) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (hasDueDay) {
                                    LoanStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Due Day",
                                        value = "Day ${currentLoan.dueDay} of month"
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                if (hasEndDate) {
                                    LoanStatCard(
                                        modifier = Modifier.weight(1f),
                                        label = "End Date",
                                        value = currentLoan.endDate!!
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Repayments.createRoute(currentLoan.id)) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, LoanColor)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = LoanColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Repayment History", color = LoanColor, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
