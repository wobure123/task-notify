package com.example.checkinmaster.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit
) {
    val task by viewModel.taskState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var reminderInput by remember(task.reminderTime) {
        mutableStateOf(task.reminderTime?.let { dateFormat.format(Date(it)) } ?: "")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = task.name, onValueChange = viewModel::updateName, label = { Text("任务名称") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.targetAppPackageName.orEmpty(), onValueChange = viewModel::updatePackage, label = { Text("包名") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.deepLinkUri.orEmpty(), onValueChange = viewModel::updateDeepLink, label = { Text("Deep Link") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.notes, onValueChange = viewModel::updateNotes, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(12.dp))
            Text(text = "优先级: ${task.priority}")
            Row { (1..3).forEach { p ->
                Button(onClick = { viewModel.updatePriority(p) }, modifier = Modifier.padding(end = 8.dp)) { Text("$p") }
            } }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = reminderInput,
                onValueChange = { reminderInput = it },
                label = { Text("提醒时间 (yyyy-MM-dd HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val parsed = kotlin.runCatching { dateFormat.parse(reminderInput)?.time }.getOrNull()
                    if (parsed != null) {
                        viewModel.updateReminderTime(parsed)
                        scope.launch { snackbarHostState.showSnackbar("已设置提醒") }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("时间格式错误") }
                    }
                }) { Text("设置提醒") }
                Spacer(Modifier.width(12.dp))
                Button(onClick = {
                    viewModel.updateReminderTime(null)
                    reminderInput = ""
                    scope.launch { snackbarHostState.showSnackbar("已清除提醒") }
                }) { Text("清除提醒") }
            }
            Spacer(Modifier.height(24.dp))
            Row {
                Button(onClick = {
                    scope.launch {
                        if (task.name.isBlank()) {
                            snackbarHostState.showSnackbar("名称不能为空")
                        } else {
                            viewModel.save()
                            snackbarHostState.showSnackbar("已保存")
                            onBack()
                        }
                    }
                }) { Text("保存") }
                Spacer(Modifier.width(12.dp))
                if (task.id != 0) {
                    Button(onClick = {
                        scope.launch {
                            viewModel.delete()
                            snackbarHostState.showSnackbar("已删除")
                            onBack()
                        }
                    }) { Text("删除") }
                }
            }
        }
    }
}
