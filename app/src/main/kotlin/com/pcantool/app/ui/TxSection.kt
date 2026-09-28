package com.pcantool.app.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pcantool.app.state.PeriodicMessageEntry

/**
 * The TX half of a view: a "TX" label, the configured periodic messages (if any), and the actual
 * table/list [content] wrapped so a right-click anywhere in it opens [SendMessageDialog] — the
 * only way to compose a message, replacing what used to be an always-visible send bar.
 */
@Composable
fun TxSection(
    periodicMessages: List<PeriodicMessageEntry>,
    onSendOnce: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray) -> Unit,
    onAddPeriodic: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray, intervalMillis: Long) -> Unit,
    onTogglePeriodic: (PeriodicMessageEntry, Boolean) -> Unit,
    onRemovePeriodic: (PeriodicMessageEntry) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        SectionLabel("TX (right-click to add a message)")
        if (periodicMessages.isNotEmpty()) {
            PeriodicMessagesPanel(periodicMessages, onTogglePeriodic, onRemovePeriodic)
        }
        ContextMenuArea(items = { listOf(ContextMenuItem("Add message…") { showDialog = true }) }) {
            Column(modifier = Modifier.weight(1f).fillMaxSize()) { content() }
        }
    }

    if (showDialog) {
        SendMessageDialog(
            onDismiss = { showDialog = false },
            onSendOnce = onSendOnce,
            onAddPeriodic = onAddPeriodic,
        )
    }
}
