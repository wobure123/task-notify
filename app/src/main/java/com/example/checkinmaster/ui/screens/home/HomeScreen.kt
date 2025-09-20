package com.example.checkinmaster.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.checkinmaster.ui.components.AddTaskFab
import com.example.checkinmaster.ui.components.TaskCard
import com.example.checkinmaster.data.model.Task

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTask: () -> Unit,
    onTaskClick: (Int) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val progress = if (total == 0) 0f else completed / total.toFloat()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { AddTaskFab(onClick = onAddTask) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (total > 0) {
                Column(Modifier.padding(16.dp)) {
                    Text(text = "今日完成: $completed / $total", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无任务，点击 + 添加") }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onToggleComplete = { viewModel.toggleComplete(task, it) },
                            onLaunchFailed = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                        )
                    }
                }
            }
        }
    }
}
