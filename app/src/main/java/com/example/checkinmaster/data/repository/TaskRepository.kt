package com.example.checkinmaster.data.repository

import com.example.checkinmaster.data.local.dao.TaskDao
import com.example.checkinmaster.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    fun getTaskById(id: Int): Flow<Task?> = taskDao.getTaskById(id)
    suspend fun upsertTask(task: Task): Long = taskDao.upsertTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun resetAllTasks() = taskDao.resetAllTasks()
}
