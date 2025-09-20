package com.example.checkinmaster

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
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
        scheduleDailyReset()
    }

    private fun scheduleDailyReset() {
        val now = LocalDateTime.now()
        var next = now.withHour(0).withMinute(5).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMillis = Duration.between(now, next).toMillis()

        val request = PeriodicWorkRequestBuilder<ResetTasksWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ResetTasksWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
