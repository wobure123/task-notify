package com.example.checkinmaster.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.checkinmaster.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = repository.getAllTasks().first()
            tasks.forEach { t ->
                val ts = t.reminderTime
                if (ts != null) {
                    AlarmScheduler.scheduleDailyExact(context, t.id, ts)
                }
            }
        }
    }
}
