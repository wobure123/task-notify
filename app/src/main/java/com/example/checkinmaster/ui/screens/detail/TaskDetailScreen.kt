package com.example.checkinmaster.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
// Removed accompanist FlowRow; using Compose Foundation FlowRow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit
) {
    val task by viewModel.taskState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    var selectedDate by remember(task.reminderTime) {
        mutableStateOf(task.reminderTime?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() })
    }
    var selectedTime by remember(task.reminderTime) {
        mutableStateOf(task.reminderTime?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalTime().withSecond(0).withNano(0) })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = task.name, onValueChange = viewModel::updateName, label = { Text("任务名称") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.targetAppPackageName.orEmpty(), onValueChange = viewModel::updatePackage, label = { Text("包名") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(text = "常用应用：一键填写包名", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickApps = listOf(
                    "淘宝" to "com.taobao.taobao",
                    "京东" to "com.jingdong.app.mall",
                    "支付宝" to "com.eg.android.AlipayGphone"
                )
                quickApps.forEach { (label, pkg) ->
                    AssistChip(onClick = { viewModel.updatePackage(pkg) }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.deepLinkUri.orEmpty(), onValueChange = viewModel::updateDeepLink, label = { Text("Deep Link") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = task.notes, onValueChange = viewModel::updateNotes, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(12.dp))
            Text(text = "优先级: ${task.priority}")
            Row {
                (1..3).forEach { p ->
                    FilterChip(
                        selected = task.priority == p,
                        onClick = { viewModel.updatePriority(p) },
                        label = { Text("$p") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val displayText = remember(selectedDate, selectedTime) {
                if (selectedDate != null && selectedTime != null) {
                    val ldt = LocalDateTime.of(selectedDate, selectedTime)
                    ldt.format(formatter)
                } else if (task.reminderTime != null) {
                    Instant.ofEpochMilli(task.reminderTime!!).atZone(zoneId).toLocalDateTime().format(formatter)
                } else {
                    "未设置提醒"
                }
            }
            Text(text = "提醒时间（每天）: $displayText", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = {
                    val today = LocalDate.now()
                    val dialog = android.app.DatePickerDialog(
                        context,
                        { _, y, m, d -> selectedDate = LocalDate.of(y, m + 1, d) },
                        (selectedDate ?: today).year,
                        (selectedDate ?: today).monthValue - 1,
                        (selectedDate ?: today).dayOfMonth
                    )
                    dialog.show()
                }) { Text("选择日期") }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = {
                    val now = LocalTime.now()
                    val base = selectedTime ?: now
                    val dialog = android.app.TimePickerDialog(
                        context,
                        { _, h, min -> selectedTime = LocalTime.of(h, min) },
                        base.hour,
                        base.minute,
                        true
                    )
                    dialog.show()
                }) { Text("选择时间") }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val date = selectedDate ?: LocalDate.now()
                    val time = selectedTime ?: LocalTime.now().withSecond(0).withNano(0)
                    val millis = LocalDateTime.of(date, time).atZone(zoneId).toInstant().toEpochMilli()
                    viewModel.updateReminderTime(millis)
                    scope.launch { snackbarHostState.showSnackbar("已设置提醒") }
                }) { Text("设置提醒") }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = {
                    viewModel.updateReminderTime(null)
                    selectedDate = null
                    selectedTime = null
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
                    OutlinedButton(onClick = {
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
