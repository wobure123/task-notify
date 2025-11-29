package com.example.checkinmaster.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
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
        scheduleNext()
        return Result.success()
    }

    private fun scheduleNext() {
        val now = LocalDateTime.now()
        val next = now.toLocalDate().plusDays(1).atTime(0, 5, 0, 0)
        val delayMillis = Duration.between(now, next).toMillis()
        val request = OneTimeWorkRequestBuilder<ResetTasksWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object { const val UNIQUE_NAME = "reset_tasks_worker" }
}
