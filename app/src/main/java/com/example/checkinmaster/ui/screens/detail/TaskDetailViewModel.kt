package com.example.checkinmaster.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.checkinmaster.data.model.Task
import com.example.checkinmaster.data.repository.TaskRepository
import com.example.checkinmaster.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.time.*
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val taskId: Int? = savedStateHandle.get<Int>("taskId")

    private val _taskState = MutableStateFlow(
        Task(
            name = "",
            targetAppPackageName = null,
            deepLinkUri = null,
            notes = "",
            priority = 1
        )
    )
    val taskState: StateFlow<Task> = _taskState

    init {
        taskId?.let { id ->
            viewModelScope.launch {
                repository.getTaskById(id).collectLatest { task ->
                    task?.let { _taskState.value = it }
                }
            }
        }
    }

    fun updateName(v: String) { _taskState.value = _taskState.value.copy(name = v) }
    fun updatePackage(v: String) { _taskState.value = _taskState.value.copy(targetAppPackageName = v.ifBlank { null }) }
    fun updateDeepLink(v: String) { _taskState.value = _taskState.value.copy(deepLinkUri = v.ifBlank { null }) }
    fun updateNotes(v: String) { _taskState.value = _taskState.value.copy(notes = v) }
    fun updatePriority(v: Int) { _taskState.value = _taskState.value.copy(priority = v) }
    fun updateReminderTime(ts: Long?) { _taskState.value = _taskState.value.copy(reminderTime = ts) }

    suspend fun save(): Int {
        var current = _taskState.value
        val rowId = repository.upsertTask(current)
        if (current.id == 0 && rowId > 0) {
            current = current.copy(id = rowId.toInt())
            _taskState.value = current
        }
        scheduleReminderIfNeeded(current)
        return current.id
    }

    private fun scheduleReminderIfNeeded(task: Task) {
        val reminder = task.reminderTime ?: return
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val timeOfDay = Instant.ofEpochMilli(reminder).atZone(zone).toLocalTime().withSecond(0).withNano(0)
        var next = now.withHour(timeOfDay.hour).withMinute(timeOfDay.minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val initialDelay = Duration.between(now, next).toMillis()

        val tag = "task_reminder_${task.id}"
        val uniqueName = tag
        // Cancel any previous works using this tag (covers legacy one-time works)
        workManager.cancelAllWorkByTag(tag)
        val data = Data.Builder().putInt(NotificationWorker.KEY_TASK_ID, task.id).build()
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()
        workManager.enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    suspend fun delete() {
        val current = _taskState.value
        if (current.id != 0) repository.deleteTask(current)
        val tag = "task_reminder_${current.id}"
        workManager.cancelAllWorkByTag(tag)
        // Also cancel unique periodic work with the same name
        workManager.cancelUniqueWork(tag)
    }
}
