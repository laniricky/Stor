package com.stor.presentation.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.stor.domain.model.Repayment
import com.stor.domain.repository.LoanRepository
import com.stor.domain.repository.RepaymentRepository
import com.stor.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddRepaymentViewModel @Inject constructor(
    private val repository: RepaymentRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun todayDate(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun submit(loanId: String, amountPaid: Double, date: String, notes: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            val repayment = Repayment(
                id = "",
                loanId = loanId,
                amountPaid = amountPaid,
                date = date,
                notes = notes.ifBlank { null },
                createdAt = date
            )

            repository.createRepayment(loanId, repayment)
                .onSuccess {
                    // Sync loans so remaining balance updates on the loan card
                    loanRepository.syncLoans()
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
    viewModel: AddRepaymentViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(viewModel.todayDate()) }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(success) {
        if (success) navController.popBackStack()
    }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Repayment", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LoanColor)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.submit(loanId, amount.toDoubleOrNull() ?: 0.0, date, notes) },
                        enabled = !isSubmitting && amount.isNotBlank()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LoanColor, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = LoanColor)
                        }
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KSH amount display (matches AddLoanScreen style)
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KSH", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = LoanColor
                        ),
                        placeholder = {
                            Text(
                                "0.00",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RepaymentFormField("Date") {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        placeholder = { Text("YYYY-MM-DD", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = LoanColor, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoanColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                RepaymentFormField("Notes (Optional)") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Monthly installment", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoanColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.submit(loanId, amount.toDoubleOrNull() ?: 0.0, date, notes) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanColor),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSubmitting && amount.isNotBlank()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundDark, strokeWidth = 2.dp)
                    } else {
                        Text("Save Repayment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RepaymentFormField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            label, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}
