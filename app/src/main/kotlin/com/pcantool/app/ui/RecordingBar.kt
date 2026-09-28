package com.pcantool.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun RecordingBar(
    isRecording: Boolean,
    recordedCount: Int,
    exportError: String?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit,
    onExport: (File) -> Unit,
) {
    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isRecording) {
            Button(onClick = onStop) { Text("Stop recording") }
        } else {
            Button(onClick = onStart) { Text("Start recording") }
        }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = onClear, enabled = !isRecording && recordedCount > 0) { Text("Clear") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
            onClick = { chooseSaveFile()?.let(onExport) },
            enabled = recordedCount > 0,
        ) { Text("Export CSV…") }
        Spacer(Modifier.width(16.dp))
        Text("Recorded: $recordedCount")
        if (exportError != null) {
            Spacer(Modifier.width(16.dp))
            Text("Export failed: $exportError", color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun chooseSaveFile(): File? {
    val dialog = FileDialog(null as Frame?, "Export recording to CSV", FileDialog.SAVE)
    dialog.file = "pcan-recording.csv"
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val file = dialog.file ?: return null
    val chosen = File(directory, file)
    return if (chosen.extension.isEmpty()) File(chosen.path + ".csv") else chosen
}
