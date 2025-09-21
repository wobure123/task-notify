package com.example.checkinmaster.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.checkinmaster.data.model.Task
import com.example.checkinmaster.data.repository.TaskRepository
import com.example.checkinmaster.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { list ->
                _tasks.value = list
            }
        }
    }

    fun toggleComplete(task: Task, newValue: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = newValue)
            repository.upsertTask(updated)
            // 不再基于完成状态取消/恢复调度；由 NotificationWorker 在运行时判断是否发送。
        }
    }
}
