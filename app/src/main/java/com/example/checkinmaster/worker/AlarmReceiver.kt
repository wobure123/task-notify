package com.example.checkinmaster.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.checkinmaster.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.app.NotificationManagerCompat

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_ALARM) return
        val taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1)
        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1)
        if (taskId == -1 || hour == -1 || minute == -1) return

        // Run logic off main thread
        CoroutineScope(Dispatchers.IO).launch {
            val task = repository.getTaskById(taskId).let { flow ->
                kotlinx.coroutines.flow.firstOrNull(flow)
            }
            // Reschedule next day regardless
            AlarmScheduler.rescheduleNextDay(context, taskId, hour, minute)

            if (task == null || task.isCompleted) return@launch

            // Reuse NotificationWorker's channel/notification logic by delegating or simple notify
            // For simplicity, trigger NotificationWorker directly via WorkManager could be done,
            // but we can also construct a small notification here. To reuse existing logic, we trigger worker.
            com.example.checkinmaster.worker.ReminderScheduler.cancel(context, taskId)
            // Use NotificationWorker one-time to show the same notification content
            androidx.work.WorkManager.getInstance(context).enqueue(
                androidx.work.OneTimeWorkRequestBuilder<com.example.checkinmaster.worker.NotificationWorker>()
                    .setInputData(androidx.work.Data.Builder().putInt(com.example.checkinmaster.worker.NotificationWorker.KEY_TASK_ID, taskId).build())
                    .build()
            )
        }
    }
}
