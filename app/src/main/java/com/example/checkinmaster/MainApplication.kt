package com.example.checkinmaster

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.checkinmaster.worker.ResetTasksWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Initialize WorkManager with HiltWorkerFactory to support @HiltWorker
        try {
            WorkManager.initialize(
                this,
                Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .setMinimumLoggingLevel(android.util.Log.VERBOSE)
                    .build()
            )
        } catch (t: Throwable) {
            Log.e("MainApplication", "WorkManager init failed", t)
        }
        try {
            scheduleDailyReset()
        } catch (t: Throwable) {
            Log.e("MainApplication", "Failed to schedule daily reset", t)
        }
    }

    private fun scheduleDailyReset() {
        val now = LocalDateTime.now()
        var next = now.withHour(0).withMinute(5).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMillis = Duration.between(now, next).toMillis()

        val request = OneTimeWorkRequestBuilder<ResetTasksWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            ResetTasksWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .setMinimumLoggingLevel(android.util.Log.VERBOSE)
        .build()
}
