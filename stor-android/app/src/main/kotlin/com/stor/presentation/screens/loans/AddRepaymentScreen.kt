package com.stor.presentation.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stor.domain.model.Repayment
import com.stor.domain.repository.RepaymentRepository
import androidx.compose.ui.graphics.Color
import com.stor.presentation.theme.LoanColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddRepaymentViewModel @Inject constructor(
    private val repository: RepaymentRepository
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun submit(loanId: String, amountPaid: Double, date: String, notes: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            val repayment = Repayment(
                id = "", // set by backend or local cache
                loanId = loanId,
                amountPaid = amountPaid,
                date = date,
                notes = notes,
                createdAt = date
            )

            repository.createRepayment(loanId, repayment)
                .onSuccess {
                    _isSubmitting.value = false
                    _success.value = true
                }
                .onFailure {
                    _isSubmitting.value = false
                    _error.value = it.message ?: "Failed to add repayment"
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepaymentScreen(
    navController: NavController,
    loanId: String,
    viewModel: AddRepaymentViewModel = viewModel()
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    var date by remember { mutableStateOf(today) }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Repayment", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount Paid") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    viewModel.submit(loanId, amount.toDoubleOrNull() ?: 0.0, date, notes) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LoanColor)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Repayment")
                }
            }
        }
    }
}
