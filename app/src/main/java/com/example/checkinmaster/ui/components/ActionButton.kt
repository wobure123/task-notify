package com.example.checkinmaster.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@Composable
fun AddTaskFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
    }
}
