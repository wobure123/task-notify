package com.example.checkinmaster.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private fun tag(taskId: Int) = "task_reminder_${taskId}"
    private fun uniqueOnce(taskId: Int) = "${tag(taskId)}_once"
    private fun uniquePeriodic(taskId: Int) = "${tag(taskId)}_periodic"

    fun scheduleDaily(context: Context, taskId: Int, reminderTimeMillis: Long) {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val time = Instant.ofEpochMilli(reminderTimeMillis).atZone(zone).toLocalTime().withSecond(0).withNano(0)
        var next = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val initialDelay = Duration.between(now, next).toMillis()

        val data = Data.Builder().putInt(NotificationWorker.KEY_TASK_ID, taskId).build()

        // First run: one-time at the next occurrence
        val once = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag(taskId))
            .build()

        // Subsequent runs: daily periodic starting 24h after the next occurrence
        val periodic = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay + TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag(taskId))
            .build()

        val wm = WorkManager.getInstance(context)
        wm.enqueueUniqueWork(uniqueOnce(taskId), ExistingWorkPolicy.REPLACE, once)
        wm.enqueueUniquePeriodicWork(uniquePeriodic(taskId), ExistingPeriodicWorkPolicy.UPDATE, periodic)
    }

    fun cancel(context: Context, taskId: Int) {
        val base = tag(taskId)
        val wm = WorkManager.getInstance(context)
        wm.cancelAllWorkByTag(base)
        wm.cancelUniqueWork(uniqueOnce(taskId))
        wm.cancelUniqueWork(uniquePeriodic(taskId))
    }
}
