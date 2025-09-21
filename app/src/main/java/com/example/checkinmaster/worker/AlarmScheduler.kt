package com.example.checkinmaster.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.*

object AlarmScheduler {
    const val ACTION_ALARM = "com.example.checkinmaster.ACTION_ALARM"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_HOUR = "extra_hour"
    const val EXTRA_MINUTE = "extra_minute"

    private fun alarmManager(context: Context) = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context, taskId: Int, hour: Int, minute: Int, flags: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
        }
        val piFlags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, taskId, intent, piFlags)
    }

    fun scheduleDailyExact(context: Context, taskId: Int, reminderTimeMillis: Long) {
        val zone = ZoneId.systemDefault()
        val time = Instant.ofEpochMilli(reminderTimeMillis).atZone(zone).toLocalTime().withSecond(0).withNano(0)
        val hour = time.hour
        val minute = time.minute

        val now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.toInstant().toEpochMilli()

        // Cancel any previous
        cancel(context, taskId)

        val pi = pendingIntent(context, taskId, hour, minute, PendingIntent.FLAG_CANCEL_CURRENT)!!
        val am = alarmManager(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                // Fallback to inexact to avoid SecurityException when exact alarms not allowed
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun rescheduleNextDay(context: Context, taskId: Int, hour: Int, minute: Int) {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val next = now.plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        val triggerAt = next.toInstant().toEpochMilli()
        val pi = pendingIntent(context, taskId, hour, minute, PendingIntent.FLAG_CANCEL_CURRENT)!!
        val am = alarmManager(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val dummyHour = 0
        val dummyMinute = 0
        val pi = pendingIntent(context, taskId, dummyHour, dummyMinute, PendingIntent.FLAG_NO_CREATE)
        if (pi != null) {
            alarmManager(context).cancel(pi)
            pi.cancel()
        }
    }
}
