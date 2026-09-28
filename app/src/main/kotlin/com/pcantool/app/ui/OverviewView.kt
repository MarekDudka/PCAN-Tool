package com.pcantool.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pcantool.core.MessageStat

/** One row per distinct CAN id, received (RX) and sent (TX) shown as separate tables. */
@Composable
fun OverviewView(rxStats: List<MessageStat>, txStats: List<MessageStat>) {
    Row(modifier = Modifier.fillMaxSize()) {
        StatsTable("RX", rxStats, modifier = Modifier.weight(1f).fillMaxHeight())
        StatsTable("TX", txStats, modifier = Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun StatsTable(title: String, stats: List<MessageStat>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderCell("Type", 90.dp)
            HeaderCell("ID (hex)", 90.dp)
            HeaderCell("DLC", 50.dp)
            HeaderCell("Data (hex)", 260.dp)
            HeaderCell("Count", 80.dp)
            HeaderCell("Cycle (ms)", 100.dp)
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(stats, key = { it.key }) { stat -> StatRow(stat) }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(text, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, modifier = Modifier.width(width))
}

@Composable
private fun StatRow(stat: MessageStat) {
    val message = stat.latest
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Cell(frameType(stat), 90.dp)
        Cell(message.idHex(), 90.dp)
        Cell(message.dlc.toString(), 50.dp)
        Cell(if (message.remote) "" else message.dataHex(), 260.dp)
        Cell(stat.count.toString(), 80.dp)
        Cell(stat.cycleTimeMicros?.let { formatMillis(it) } ?: "-", 100.dp)
    }
}

@Composable
private fun Cell(text: String, width: Dp) {
    Text(text, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false, modifier = Modifier.width(width))
}

private fun frameType(stat: MessageStat): String = when {
    stat.latest.extended && stat.latest.remote -> "EXT/RTR"
    stat.latest.extended -> "EXT"
    stat.latest.remote -> "STD/RTR"
    else -> "STD"
}

private fun formatMillis(micros: Long): String {
    val whole = micros / 1000
    val fraction = micros % 1000
    return "$whole.${fraction.toString().padStart(3, '0')}"
}
