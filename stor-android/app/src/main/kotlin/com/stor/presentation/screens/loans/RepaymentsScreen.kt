package com.stor.presentation.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.stor.domain.model.Repayment
import com.stor.domain.repository.LoanRepository
import com.stor.domain.repository.RepaymentRepository
import com.stor.presentation.navigation.Screen
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepaymentsViewModel @Inject constructor(
    private val repository: RepaymentRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _repayments = MutableStateFlow<List<Repayment>>(emptyList())
    val repayments: StateFlow<List<Repayment>> = _repayments

    private val _totalPaid = MutableStateFlow(0.0)
    val totalPaid: StateFlow<Double> = _totalPaid

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRepayments(loanId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncRepayments(loanId)
            // Also sync the loan itself so remaining balance updates
            loanRepository.syncLoans()
            repository.getRepayments(loanId).collect { list ->
                _repayments.value = list.sortedByDescending { it.date }
                _totalPaid.value = list.sumOf { it.amountPaid }
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepaymentsScreen(
    navController: NavController,
    loanId: String,
    viewModel: RepaymentsViewModel = hiltViewModel()
) {
    val repayments by viewModel.repayments.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadRepayments(loanId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Repayments", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LoanColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddRepayment.createRoute(loanId)) },
                containerColor = LoanColor,
                contentColor = BackgroundDark,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Repayment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Summary header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = LoanColor,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Repaid", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Text(formatKsh(totalPaid), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = LoanColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${repayments.size} payment${if (repayments.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading && repayments.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LoanColor)
                    }
                }
            } else if (repayments.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No repayments yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap + to record your first payment", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "Payment History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                items(repayments) { repayment ->
                    RepaymentRow(repayment)
                }
            }
        }
    }
}

@Composable
fun RepaymentRow(repayment: Repayment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = CircleShape,
                    color = LoanColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = LoanColor, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(
                        text = if (repayment.notes.isNullOrBlank()) "Repayment" else repayment.notes,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(repayment.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Text(
                text = formatKsh(repayment.amountPaid),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LoanColor
            )
        }
    }
}
