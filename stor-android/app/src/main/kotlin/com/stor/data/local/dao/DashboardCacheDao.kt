package com.stor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stor.data.local.entities.DashboardCacheEntity

@Dao
interface DashboardCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: DashboardCacheEntity)

    @Query("SELECT * FROM dashboard_cache WHERE id = 1")
    suspend fun get(): DashboardCacheEntity?
}
