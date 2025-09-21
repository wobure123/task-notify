package com.example.checkinmaster.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.checkinmaster.data.model.Task
import com.example.checkinmaster.data.repository.TaskRepository
import com.example.checkinmaster.worker.NotificationWorker
import com.example.checkinmaster.worker.ReminderScheduler
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

    // Work scheduling is handled via ReminderScheduler

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
        val reminder = task.reminderTime
        if (reminder == null) {
            ReminderScheduler.cancel(context, task.id)
        } else {
            ReminderScheduler.scheduleDaily(context, task.id, reminder)
        }
    }

    suspend fun delete() {
        val current = _taskState.value
        if (current.id != 0) repository.deleteTask(current)
        ReminderScheduler.cancel(context, current.id)
    }
}
