package com.stor.presentation.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stor.presentation.navigation.Screen
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.LoanColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedLoansScreen(
    navController: NavController,
    viewModel: LoansViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val archivedLoans = state.loans.filter { it.remainingBalance <= 0 }
    val totalPaidOff = archivedLoans.sumOf { loan -> loan.originalAmount }

    LaunchedEffect(Unit) {
        viewModel.sync()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Archived Loans", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
                        Text("Total Paid Off", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatKsh(totalPaidOff), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LoanColor)
                        Text(
                            "across ${archivedLoans.size} fully paid loans",
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
            } else if (archivedLoans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No archived loans", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                }
            } else {
                items(items = archivedLoans, key = { loan -> loan.id }) { loan ->
                    LoanCard(
                        loan = loan,
                        onClick = { navController.navigate(Screen.LoanDetail.createRoute(loan.id)) },
                        onViewRepayments = { navController.navigate(Screen.Repayments.createRoute(loan.id)) },
                        onDelete = { viewModel.delete(loan.id) }
                    )
                }
            }
        }
    }
}
