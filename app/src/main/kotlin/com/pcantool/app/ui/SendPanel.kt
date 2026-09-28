package com.pcantool.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Manual "send one frame" control, enabled only while connected. */
@Composable
fun SendPanel(
    enabled: Boolean,
    sendError: String?,
    onSend: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray) -> Unit,
) {
    var idText by remember { mutableStateOf("100") }
    var dataText by remember { mutableStateOf("00 00 00 00 00 00 00 00") }
    var extended by remember { mutableStateOf(false) }
    var remote by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = idText,
            onValueChange = { idText = it },
            label = { Text("Send ID (hex)") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.width(140.dp),
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = dataText,
            onValueChange = { dataText = it },
            label = { Text("Data (hex bytes)") },
            singleLine = true,
            enabled = enabled && !remote,
            modifier = Modifier.width(260.dp),
        )
        Spacer(Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = extended, onCheckedChange = { extended = it }, enabled = enabled)
            Text("Extended")
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = remote, onCheckedChange = { remote = it }, enabled = enabled)
            Text("Remote")
        }
        Spacer(Modifier.width(16.dp))
        Button(
            enabled = enabled,
            onClick = {
                val id = idText.toLongOrNull(16)
                val data = if (remote) ByteArray(0) else parseHexBytes(dataText)
                validationError = when {
                    id == null -> "Invalid id"
                    !remote && data == null -> "Invalid data (use hex byte pairs, e.g. 01 02 FF)"
                    !remote && (data?.size ?: 0) > 8 -> "At most 8 data bytes"
                    else -> null
                }
                if (validationError == null && id != null) {
                    onSend(id, extended, remote, data ?: ByteArray(0))
                }
            },
        ) { Text("Send") }
        val error = validationError ?: sendError
        if (error != null) {
            Spacer(Modifier.width(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun parseHexBytes(text: String): ByteArray? {
    val tokens = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return ByteArray(0)
    val bytes = mutableListOf<Byte>()
    for (token in tokens) {
        val value = token.toIntOrNull(16) ?: return null
        if (value !in 0..255) return null
        bytes.add(value.toByte())
    }
    return bytes.toByteArray()
}
