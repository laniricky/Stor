package com.stor.di

import android.content.Context
import androidx.room.Room
import com.stor.data.local.StorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StorDatabase {
        return Room.databaseBuilder(
            context,
            StorDatabase::class.java,
            "stor_database"
        ).build()
    }
}
