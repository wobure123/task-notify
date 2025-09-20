package com.example.checkinmaster.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.checkinmaster.MainActivity
import com.example.checkinmaster.R
import com.example.checkinmaster.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt(KEY_TASK_ID, -1)
        if (id == -1) return Result.success()
        val task = repository.getTaskById(id).firstOrNull()
        if (task == null || task.isCompleted) return Result.success()
        createChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("open_task_id", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } ?: PendingIntent.getActivity(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("待签到任务")
            .setContentText(task.name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val KEY_TASK_ID = "TASK_ID"
        const val CHANNEL_ID = "TASK_REMINDER_CHANNEL"
    }
}
