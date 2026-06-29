package com.stor.presentation.screens.more.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.repository.ExpenseRepository
import com.stor.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

@HiltViewModel
class ExportReportsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() { _error.value = null }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val expenses = expenseRepository.getExpenses().first()
                val income = incomeRepository.getIncome().first()
                
                val fileName = "Stor_Report_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)
                
                FileWriter(file).use { writer ->
                    // Write Income
                    writer.append("--- INCOME ---\n")
                    writer.append("ID,Source,Amount,Date,Notes\n")
                    income.forEach { inc ->
                        writer.append("${inc.id},${inc.source},${inc.amount},${inc.date},${inc.notes ?: ""}\n")
                    }
                    
                    writer.append("\n--- EXPENSES ---\n")
                    writer.append("ID,Title,Amount,Category,Payment Method,Date,Notes\n")
                    expenses.forEach { exp ->
                        writer.append("${exp.id},${exp.title},${exp.amount},${exp.category},${exp.paymentMethod},${exp.date},${exp.notes ?: ""}\n")
                    }
                }
                
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Share Report"))
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to export data: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
