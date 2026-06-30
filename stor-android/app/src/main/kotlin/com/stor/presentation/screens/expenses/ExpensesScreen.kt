package com.stor.presentation.screens.expenses

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.extended.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.stor.domain.model.Expense
import com.stor.presentation.navigation.Screen
import com.stor.presentation.screens.dashboard.formatKsh
import com.stor.presentation.theme.*

private val categories = listOf("Food","Transport","Rent","Utilities","Shopping","Entertainment","Health","Education","Other")

private fun categoryIcon(cat: String): ImageVector = when (cat) {
    "Food" -> Icons.Default.Restaurant
    "Transport" -> Icons.Default.DirectionsCar
    "Rent" -> Icons.Default.Home
    "Utilities" -> Icons.Default.Power
    "Shopping" -> Icons.Default.ShoppingCart
    "Entertainment" -> Icons.Default.PlayCircle
    "Health" -> Icons.Default.Favorite
    "Education" -> Icons.Default.MenuBook
    else -> Icons.Default.MoreHoriz
}

private fun categoryColor(cat: String): Color = when (cat) {
    "Food" -> Color(0xFFF97316)
    "Transport" -> Color(0xFF3B82F6)
    "Rent" -> Color(0xFF8B5CF6)
    "Utilities" -> Color(0xFFF59E0B)
    "Shopping" -> Color(0xFFEC4899)
    "Entertainment" -> Color(0xFF06B6D4)
    "Health" -> Color(0xFF10B981)
    "Education" -> Color(0xFF6366F1)
    else -> Color(0xFF9CA3AF)
}

@Composable
fun ExpensesScreen(
    navController: NavController,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sync()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.route) },
                containerColor = TealPrimary, contentColor = BackgroundDark, shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Expense") }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expenses", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Row {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TealPrimary)
                        }
                        IconButton(onClick = { viewModel.sync() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TealPrimary)
                        }
                    }
                }
                if (showSearch) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        placeholder = { Text("Search expenses…") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = state.selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor(cat),
                                selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (state.expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No expenses found", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Offline banner
                val hasUnsynced = state.expenses.any { !it.isSynced }
                if (hasUnsynced) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null,
                                tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text("Some items are pending sync",
                                fontSize = 12.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }
                item {
                    val total = state.expenses.sumOf { expense -> expense.amount }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ExpenseColor.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (${state.expenses.size} items)",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            Text(formatKsh(total), fontWeight = FontWeight.Bold, color = ExpenseColor)
                        }
                    }
                }
                items(items = state.expenses, key = { expense -> expense.id }) { expense ->
                    ExpenseCard(expense = expense, onDelete = { viewModel.deleteExpense(expense.id) })
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, onDelete: () -> Unit) {
    val color = categoryColor(expense.category)
    val icon = categoryIcon(expense.category)
    var showConfirm by remember { mutableStateOf(false) }
    val pendingColor = Color(0xFFF59E0B)

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!expense.isSynced) pendingColor.copy(alpha = 0.07f) else CardBackground
        )
    ) {
        // Amber left-border stripe for unsynced items
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (!expense.isSynced) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .heightIn(min = 80.dp)
                            .background(pendingColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    )
                }
                Row(modifier = Modifier.padding(16.dp).weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(expense.title, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(expense.category, fontSize = 12.sp, color = color)
                            Text("·", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            Text(expense.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!expense.isSynced) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Pending sync",
                                    tint = pendingColor, modifier = Modifier.size(14.dp))
                            }
                            Text(formatKsh(expense.amount), fontWeight = FontWeight.Bold, color = ExpenseColor)
                        }
                        Text(expense.paymentMethod, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                            tint = ExpenseColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            if (showConfirm) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delete?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f))
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
