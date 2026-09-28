package com.pcantool.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.pcantool.app.state.PeriodicMessageEntry

/** Configured cyclic TX messages, each independently switchable on/off. */
@Composable
fun PeriodicMessagesPanel(
    entries: List<PeriodicMessageEntry>,
    onToggle: (PeriodicMessageEntry, Boolean) -> Unit,
    onRemove: (PeriodicMessageEntry) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                Switch(checked = entry.enabled, onCheckedChange = { onToggle(entry, it) })
                Spacer(Modifier.width(8.dp))
                Text(
                    describe(entry),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onRemove(entry) }) { Text("Remove") }
            }
        }
    }
}

private fun describe(entry: PeriodicMessageEntry): String {
    val idHex = entry.id.toString(16).uppercase().padStart(if (entry.extended) 8 else 3, '0')
    val payload = if (entry.remote) "RTR" else entry.data.toSpacedHex()
    return "$idHex  $payload  every ${entry.intervalMillis} ms"
}
