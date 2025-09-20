package com.example.checkinmaster.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.checkinmaster.data.local.dao.TaskDao
import com.example.checkinmaster.data.model.Task

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
