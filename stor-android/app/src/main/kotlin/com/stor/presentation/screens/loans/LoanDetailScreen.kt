package com.stor.presentation.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.LoanColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _loan = MutableStateFlow<Loan?>(null)
    val loan: StateFlow<Loan?> = _loan

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadLoan(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loan.value = repository.getLoanById(id)
            _isLoading.value = false
        }
    }
}

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
        topBar = {
            TopAppBar(
                title = { Text("Loan Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanColor)
            }
        } else if (loan == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loan not found", color = Color.Gray)
            }
        } else {
            val currentLoan = loan!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(currentLoan.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Lender: ${currentLoan.lender}", fontSize = 16.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Original Amount", fontSize = 14.sp, color = Color.Gray)
                        Text(formatKsh(currentLoan.originalAmount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Remaining", fontSize = 14.sp, color = Color.Gray)
                        Text(formatKsh(currentLoan.remainingBalance), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LoanColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = (currentLoan.percentagePaid / 100).coerceIn(0.0, 1.0).toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = LoanColor,
                    trackColor = LoanColor.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${String.format("%.1f", currentLoan.percentagePaid)}% paid", fontSize = 14.sp, color = Color.Gray)
                    Text("Status: ${currentLoan.status}", fontSize = 14.sp, color = Color.Gray)
                }

            }
        }
    }
}
