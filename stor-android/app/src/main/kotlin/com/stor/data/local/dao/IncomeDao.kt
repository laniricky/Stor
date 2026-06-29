package com.stor.data.local.dao

import androidx.room.*
import com.stor.data.local.entities.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    suspend fun getAllIncomeList(): List<IncomeEntity>

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: String): IncomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(incomes: List<IncomeEntity>)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteIncomeById(id: String)
    
    @Query("DELETE FROM income")
    suspend fun clearAll()

    @Query("SELECT * FROM income WHERE is_synced = 0")
    suspend fun getUnsynced(): List<IncomeEntity>

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun hardDelete(id: String)
}
