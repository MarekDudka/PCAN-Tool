package com.pcantool.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pcantool.app.state.PeriodicMessageEntry
import com.pcantool.core.CanMessage

/** Chronological logs of filtered messages: received (RX) on top, sent (TX) below. */
@Composable
fun TraceView(
    rxMessages: List<CanMessage>,
    txMessages: List<CanMessage>,
    periodicMessages: List<PeriodicMessageEntry>,
    onSendOnce: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray) -> Unit,
    onAddPeriodic: (id: Long, extended: Boolean, remote: Boolean, data: ByteArray, intervalMillis: Long) -> Unit,
    onTogglePeriodic: (PeriodicMessageEntry, Boolean) -> Unit,
    onRemovePeriodic: (PeriodicMessageEntry) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            SectionLabel("RX")
            TraceList(rxMessages, modifier = Modifier.fillMaxSize())
        }
        HorizontalDivider()
        TxSection(
            periodicMessages = periodicMessages,
            onSendOnce = onSendOnce,
            onAddPeriodic = onAddPeriodic,
            onTogglePeriodic = onTogglePeriodic,
            onRemovePeriodic = onRemovePeriodic,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            TraceList(txMessages, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun TraceList(messages: List<CanMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    Column(modifier = modifier) {
        TraceHeaderRow()
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(messages, key = { index, _ -> index }) { index, message ->
                TraceRow(index + 1, message)
            }
        }
    }
}

@Composable
private fun TraceHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderCell("#", 60.dp)
        HeaderCell("Time (ms)", 100.dp)
        HeaderCell("Type", 90.dp)
        HeaderCell("ID (hex)", 90.dp)
        HeaderCell("DLC", 50.dp)
        HeaderCell("Data (hex)", 260.dp)
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(text, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, modifier = Modifier.width(width))
}

@Composable
private fun TraceRow(index: Int, message: CanMessage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Cell(index.toString(), 60.dp)
        Cell(formatMillis(message.timestampMicros), 100.dp)
        Cell(frameType(message), 90.dp)
        Cell(message.idHex(), 90.dp)
        Cell(message.dlc.toString(), 50.dp)
        Cell(if (message.remote) "" else message.dataHex(), 260.dp)
    }
}

@Composable
private fun Cell(text: String, width: Dp) {
    Text(text, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false, modifier = Modifier.width(width))
}

private fun frameType(message: CanMessage): String = when {
    message.extended && message.remote -> "EXT/RTR"
    message.extended -> "EXT"
    message.remote -> "STD/RTR"
    else -> "STD"
}

private fun formatMillis(micros: Long): String {
    val whole = micros / 1000
    val fraction = micros % 1000
    return "$whole.${fraction.toString().padStart(3, '0')}"
}
