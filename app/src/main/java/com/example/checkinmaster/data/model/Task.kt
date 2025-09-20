package com.example.checkinmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAppPackageName: String?,
    val deepLinkUri: String?,
    val notes: String,
    val priority: Int, // 1 (低), 2 (中), 3 (高)
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null // Unix 毫秒时间戳
)
