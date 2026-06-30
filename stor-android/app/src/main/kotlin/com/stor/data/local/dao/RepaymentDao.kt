package com.stor.data.local.dao

import androidx.room.*
import com.stor.data.local.entities.RepaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {
    @Query("SELECT * FROM repayments WHERE loan_id = :loanId ORDER BY date DESC")
    fun getRepaymentsForLoan(loanId: String): Flow<List<RepaymentEntity>>

    @Query("SELECT * FROM repayments WHERE id = :id")
    suspend fun getRepaymentById(id: String): RepaymentEntity?

    @Query("SELECT SUM(amount_paid) FROM repayments WHERE loan_id = :loanId")
    suspend fun getTotalRepaidForLoan(loanId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: RepaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayments(repayments: List<RepaymentEntity>)

    @Delete
    suspend fun deleteRepayment(repayment: RepaymentEntity)

    @Query("DELETE FROM repayments WHERE id = :id")
    suspend fun deleteRepaymentById(id: String)
    
    @Query("DELETE FROM repayments")
    suspend fun clearAll()
}
