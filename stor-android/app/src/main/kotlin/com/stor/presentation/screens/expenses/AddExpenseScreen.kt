package com.stor.presentation.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stor.presentation.theme.*

private val categories = listOf(
    "Food", "Transport", "Rent", "Utilities", "Shopping",
    "Entertainment", "Health", "Education", "Other"
)

private val paymentMethods = listOf("Cash", "M-Pesa", "Card", "Bank Transfer", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var date by remember { mutableStateOf(viewModel.todayDate()) }
    var notes by remember { mutableStateOf("") }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    // Navigate back on success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navController.popBackStack()
    }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TealPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveExpense(
                                title = title,
                                amount = amountText.toDoubleOrNull() ?: 0.0,
                                category = selectedCategory,
                                paymentMethod = selectedPaymentMethod,
                                date = date,
                                notes = notes,
                                description = null
                            )
                        }
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TealPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = TealPrimary)
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
            // Amount display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KSH", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        placeholder = {
                            Text(
                                "0.00",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Form fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                FormField("Title") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Lunch", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Category
                FormField("Category") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { showCategorySheet = true }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedCategory.ifBlank { "Select category" },
                            color = if (selectedCategory.isBlank())
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Payment Method
                FormField("Payment Method") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { showPaymentSheet = true }
                            .padding(16.dp)
                    ) {
                        Text(text = selectedPaymentMethod, color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // Date
                FormField("Date") {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TealPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Notes
                FormField("Notes (Optional)") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add notes", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.saveExpense(
                            title = title,
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory,
                            paymentMethod = selectedPaymentMethod,
                            date = date,
                            notes = notes,
                            description = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundDark, strokeWidth = 2.dp)
                    } else {
                        Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Category bottom sheet
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            containerColor = CardBackground
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Category", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onSurface)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    items(categories) { cat ->
                        val selected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) TealPrimary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (selected) TealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat; showCategorySheet = false }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cat,
                                fontSize = 13.sp,
                                color = if (selected) BackgroundDark else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Payment method bottom sheet
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = CardBackground
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onSurface)
                paymentMethods.forEach { method ->
                    val selected = method == selectedPaymentMethod
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method; showPaymentSheet = false }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(method, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = TealPrimary)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp))
        content()
    }
}
