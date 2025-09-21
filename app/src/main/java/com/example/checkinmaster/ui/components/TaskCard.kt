package com.example.checkinmaster.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import com.example.checkinmaster.data.model.Task

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    onLaunchFailed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete(it) },
                    modifier = Modifier.scale(1.15f)
                )
            }
            if (task.notes.isNotBlank()) {
                Text(
                    text = task.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = { launchTask(context, task, onLaunchFailed) }) {
                    Text("去完成")
                }
                PriorityTag(priority = task.priority)
            }
        }
    }
}

@Composable
private fun PriorityTag(priority: Int) {
    val (text, color) = when (priority) {
        3 -> "高" to Color(0xFFFE5167)
        2 -> "中" to Color(0xFFFF9C00)
        else -> "低" to Color(0xFF999999)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) { Text(text = text, color = color, style = MaterialTheme.typography.labelSmall) }
}

private fun launchTask(context: Context, task: Task, onLaunchFailed: (String) -> Unit) {
    val deepLink = task.deepLinkUri
    val pkg = task.targetAppPackageName
    try {
        if (!deepLink.isNullOrBlank()) {
            val uri = Uri.parse(deepLink)
            val scheme = uri.scheme?.lowercase()
            if (!pkg.isNullOrBlank() && (scheme == "http" || scheme == "https")) {
                // 优先尝试用指定包名在 App 内打开该 H5 链接
                val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (appIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(appIntent)
                    return
                }
            }
            // 退回普通 VIEW（可能走浏览器或系统选择器）
            val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (viewIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(viewIntent)
                return
            } else if (!pkg.isNullOrBlank()) {
                // 深链不可处理，尝试直接启动目标应用
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return
                }
            }
        }
        if (!pkg.isNullOrBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                return
            } else {
                // 尝试跳到应用商店
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (marketIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(marketIntent)
                    return
                }
            }
        }
        onLaunchFailed("无法打开：未找到可处理的 DeepLink 或包名不可启动")
    } catch (e: ActivityNotFoundException) {
        onLaunchFailed("应用未安装或链接无效")
    }
}
