package com.stor.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.stor.domain.repository.ExpenseRepository
import com.stor.domain.repository.IncomeRepository
import com.stor.domain.repository.LoanRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that uploads all locally-saved (isSynced = false) records
 * to the server whenever the device regains a network connection.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            expenseRepository.syncExpenses()
            incomeRepository.syncIncome()
            loanRepository.syncLoans()
            Result.success()
        } catch (e: Exception) {
            // Retry once on failure
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "stor_sync_worker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
