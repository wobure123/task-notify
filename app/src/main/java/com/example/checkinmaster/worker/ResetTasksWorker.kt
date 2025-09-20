package com.example.checkinmaster.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.checkinmaster.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ResetTasksWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        repository.resetAllTasks()
        return Result.success()
    }
    companion object { const val UNIQUE_NAME = "reset_tasks_worker" }
}
