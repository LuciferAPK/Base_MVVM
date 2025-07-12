package com.example.basemvvm.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

//@InstallIn(SingletonComponent::class)
//@Module
//class StorageRoom {
//
//    @Singleton
//    @Provides
//    fun appDatabase(
//        @ApplicationContext context: Context,
//        @DatabaseInfo dataName: String
//    ): AppDatabase {
//        return Room.databaseBuilder(context, AppDatabase::class.java, dataName)
//            .addMigrations()
//            .fallbackToDestructiveMigration(true)
//            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
//            .setQueryExecutor(Executors.newSingleThreadExecutor())
//            .build()
//    }
//
//    @Singleton
//    @Provides
//    @DatabaseInfo
//    fun databaseName(): String {
//        return "my_app.db"
//    }
//}