package com.pcantool.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Message composer opened from the TX panel's right-click menu. Submitting either sends the
 * frame once immediately, or — when "Send periodically" is checked — registers it as a new
 * (initially disabled) cyclic entry that the TX panel's own switch then turns on/off.
 */
@Composable
fun SendMessageDialog(
    onDismiss: () -> Unit,
    onSendOnce: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray) -> Unit,
    onAddPeriodic: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray, intervalMillis: Long) -> Unit,
) {
    var idText by remember { mutableStateOf("100") }
    var dataText by remember { mutableStateOf("00 00 00 00 00 00 00 00") }
    var extended by remember { mutableStateOf(false) }
    var remote by remember { mutableStateOf(false) }
    var periodic by remember { mutableStateOf(false) }
    var intervalText by remember { mutableStateOf("100") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(20.dp).widthIn(min = 360.dp)) {
                Text("Add TX message", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = idText,
                    onValueChange = { idText = it },
                    label = { Text("ID (hex)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataText,
                    onValueChange = { dataText = it },
                    label = { Text("Data (hex bytes)") },
                    singleLine = true,
                    enabled = !remote,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = extended, onCheckedChange = { extended = it })
                    Text("Extended")
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = remote, onCheckedChange = { remote = it })
                    Text("Remote")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = periodic, onCheckedChange = { periodic = it })
                    Text("Send periodically")
                }
                if (periodic) {
                    OutlinedTextField(
                        value = intervalText,
                        onValueChange = { intervalText = it },
                        label = { Text("Interval (ms)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val id = idText.toLongOrNull(16)
                        val data = if (remote) ByteArray(0) else parseHexBytes(dataText)
                        val interval = if (periodic) intervalText.toLongOrNull() else null
                        error = when {
                            id == null -> "Invalid id"
                            !remote && data == null -> "Invalid data (use hex byte pairs, e.g. 01 02 FF)"
                            !remote && (data?.size ?: 0) > 8 -> "At most 8 data bytes"
                            periodic && (interval == null || interval <= 0) -> "Invalid interval"
                            else -> null
                        }
                        if (error == null && id != null) {
                            val bytes = data ?: ByteArray(0)
                            if (periodic) {
                                onAddPeriodic(id, extended, remote, bytes, interval!!)
                            } else {
                                onSendOnce(id, extended, remote, bytes)
                            }
                            onDismiss()
                        }
                    }) { Text(if (periodic) "Add" else "Send") }
                }
            }
        }
    }
}
