package com.pcantool.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.pcantool.app.state.ConnectionState
import com.pcantool.pcanbasic.PcanBaudRate
import com.pcantool.pcanbasic.PcanChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionBar(
    connectionState: ConnectionState,
    selectedChannel: PcanChannel,
    onChannelSelected: (PcanChannel) -> Unit,
    selectedBaudRate: PcanBaudRate,
    onBaudRateSelected: (PcanBaudRate) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = connectionState is ConnectionState.Connected
    val connecting = connectionState is ConnectionState.Connecting

    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnumDropdown(
            label = "Channel",
            options = PcanChannel.entries,
            selected = selectedChannel,
            optionLabel = { it.label },
            enabled = !connected && !connecting,
            onSelected = onChannelSelected,
        )
        Spacer(Modifier.width(12.dp))
        EnumDropdown(
            label = "Baud rate",
            options = PcanBaudRate.entries,
            selected = selectedBaudRate,
            optionLabel = { it.label },
            enabled = !connected && !connecting,
            onSelected = onBaudRateSelected,
        )
        Spacer(Modifier.width(12.dp))
        if (connected || connecting) {
            Button(onClick = onDisconnect, enabled = !connecting) { Text(if (connecting) "Connecting…" else "Disconnect") }
        } else {
            Button(onClick = onConnect) { Text("Connect") }
        }
        Spacer(Modifier.width(16.dp))
        val statusText = when (connectionState) {
            is ConnectionState.Disconnected -> "Disconnected"
            is ConnectionState.Connecting -> "Connecting…"
            is ConnectionState.Connected -> "Connected: ${connectionState.channel.label}"
            is ConnectionState.Error -> "Error: ${connectionState.message}"
        }
        val color = if (connectionState is ConnectionState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        Text(statusText, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).width(200.dp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
