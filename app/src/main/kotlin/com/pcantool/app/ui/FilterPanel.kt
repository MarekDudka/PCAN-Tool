package com.pcantool.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
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
import com.pcantool.core.MessageFilter

/**
 * Editable id-range/frame-kind/free-text filter, applied on every change (no separate "Apply"
 * step) so the trace view reacts immediately, matching PCAN-View's own filter behavior.
 */
@Composable
fun FilterPanel(filter: MessageFilter, onFilterChanged: (MessageFilter) -> Unit) {
    var fromText by remember(filter) { mutableStateOf(filter.fromId.toString(16).uppercase()) }
    var toText by remember(filter) { mutableStateOf(filter.toId.toString(16).uppercase()) }

    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = fromText,
            onValueChange = { text ->
                fromText = text
                text.toLongOrNull(16)?.let { onFilterChanged(filter.copy(fromId = it)) }
            },
            label = { Text("From (hex)") },
            singleLine = true,
            modifier = Modifier.width(140.dp),
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = toText,
            onValueChange = { text ->
                toText = text
                text.toLongOrNull(16)?.let { onFilterChanged(filter.copy(toId = it)) }
            },
            label = { Text("To (hex)") },
            singleLine = true,
            modifier = Modifier.width(140.dp),
        )
        Spacer(Modifier.width(16.dp))
        LabeledCheckbox("Standard", filter.allowStandard) { onFilterChanged(filter.copy(allowStandard = it)) }
        LabeledCheckbox("Extended", filter.allowExtended) { onFilterChanged(filter.copy(allowExtended = it)) }
        LabeledCheckbox("Data", filter.allowData) { onFilterChanged(filter.copy(allowData = it)) }
        LabeledCheckbox("Remote", filter.allowRemote) { onFilterChanged(filter.copy(allowRemote = it)) }
        Spacer(Modifier.width(16.dp))
        OutlinedTextField(
            value = filter.searchText,
            onValueChange = { onFilterChanged(filter.copy(searchText = it)) },
            label = { Text("Search id/data") },
            singleLine = true,
            modifier = Modifier.width(160.dp),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = {
            fromText = "0"
            toText = "1FFFFFFF"
            onFilterChanged(MessageFilter.ACCEPT_ALL)
        }) { Text("Reset") }
    }
}

@Composable
private fun LabeledCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
