package com.stor.presentation.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stor.domain.model.Repayment
import com.stor.domain.repository.RepaymentRepository
import com.stor.presentation.navigation.Screen
import androidx.compose.ui.graphics.Color
import com.stor.presentation.theme.CardBackground
import com.stor.presentation.theme.LoanColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RepaymentsViewModel @Inject constructor(
    private val repository: RepaymentRepository
) : ViewModel() {

    private val _repayments = MutableStateFlow<List<Repayment>>(emptyList())
    val repayments: StateFlow<List<Repayment>> = _repayments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRepayments(loanId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncRepayments(loanId)
            repository.getRepayments(loanId).collect {
                _repayments.value = it
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
    viewModel: RepaymentsViewModel = viewModel()
) {
    val repayments by viewModel.repayments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadRepayments(loanId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repayments", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddRepayment.createRoute(loanId)) },
                containerColor = LoanColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Repayment", tint = CardBackground)
            }
        }
    ) { padding ->
        if (isLoading && repayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LoanColor)
            }
        } else if (repayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No repayments yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(repayments) { repayment ->
                    RepaymentCard(repayment)
                }
            }
        }
    }
}

@Composable
fun RepaymentCard(repayment: Repayment) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currencyFormatter.format(repayment.amountPaid),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoanColor
                )
                Text(text = repayment.date, fontSize = 14.sp, color = Color.Gray)
            }
            if (!repayment.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = repayment.notes, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
