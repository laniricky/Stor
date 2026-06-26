package com.stor.search

import com.stor.expenses.ExpenseRepository
import com.stor.income.IncomeRepository
import com.stor.loans.LoanRepository
import com.stor.repayments.RepaymentRepository
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SearchResults(
    val expenses: List<SearchResult>,
    val income: List<SearchResult>,
    val loans: List<SearchResult>,
    val repayments: List<SearchResult>
)

@Serializable
data class SearchResult(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val date: String
)

class SearchService {
    private val expenseRepo = ExpenseRepository()
    private val incomeRepo = IncomeRepository()
    private val loanRepo = LoanRepository()
    private val repaymentRepo = RepaymentRepository()

    fun search(userId: String, query: String, type: String?): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), emptyList(), emptyList(), emptyList())
        val uuid = UUID.fromString(userId)

        val expenses = if (type == null || type == "expense") {
            expenseRepo.findForSearch(uuid, query).map {
                SearchResult(it.id, "expense", it.title, it.category, it.amount, it.date)
            }
        } else emptyList()

        val income = if (type == null || type == "income") {
            incomeRepo.findForSearch(uuid, query).map {
                SearchResult(it.id, "income", it.source, "Income", it.amount, it.date)
            }
        } else emptyList()

        val loans = if (type == null || type == "loan") {
            loanRepo.findForSearch(uuid, query).map {
                SearchResult(it.id, "loan", it.name, it.lender, it.remainingBalance, it.startDate)
            }
        } else emptyList()

        val repayments = if (type == null || type == "repayment") {
            repaymentRepo.findForSearch(uuid, query).map {
                SearchResult(it.id, "repayment", "Repayment", it.notes ?: "", it.amountPaid, it.date)
            }
        } else emptyList()

        return SearchResults(expenses, income, loans, repayments)
    }
}
