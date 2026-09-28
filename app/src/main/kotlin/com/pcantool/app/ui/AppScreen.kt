package com.pcantool.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pcantool.app.state.AppViewModel
import com.pcantool.app.state.ConnectionState

private enum class ViewTab(val label: String) { TRACE("Trace"), OVERVIEW("Overview") }

@Composable
fun AppScreen(viewModel: AppViewModel) {
    var tab by remember { mutableStateOf(ViewTab.OVERVIEW) }

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectionBar(
            connectionState = viewModel.connectionState,
            selectedChannel = viewModel.selectedChannel,
            onChannelSelected = { viewModel.selectedChannel = it },
            selectedBaudRate = viewModel.selectedBaudRate,
            onBaudRateSelected = { viewModel.selectedBaudRate = it },
            onConnect = viewModel::connect,
            onDisconnect = viewModel::disconnect,
        )
        HorizontalDivider()
        SendPanel(
            enabled = viewModel.connectionState is ConnectionState.Connected,
            sendError = viewModel.lastSendError,
            onSend = viewModel::sendMessage,
        )
        HorizontalDivider()
        FilterPanel(filter = viewModel.filter, onFilterChanged = { viewModel.filter = it })
        HorizontalDivider()
        RecordingBar(
            isRecording = viewModel.isRecording,
            recordedCount = viewModel.recordedCount,
            exportError = viewModel.lastExportError,
            onStart = viewModel::startRecording,
            onStop = viewModel::stopRecording,
            onClear = viewModel::clearRecording,
            onExport = viewModel::exportRecordingToCsv,
        )
        HorizontalDivider()
        TabRow(selectedTabIndex = tab.ordinal) {
            ViewTab.entries.forEach { entry ->
                Tab(selected = tab == entry, onClick = { tab = entry }, text = { Text(entry.label) })
            }
        }
        when (tab) {
            ViewTab.TRACE -> TraceView(viewModel.rxTraceMessages, viewModel.txTraceMessages)
            ViewTab.OVERVIEW -> OverviewView(
                rxStats = viewModel.rxStats.values.sortedBy { it.latest.id },
                txStats = viewModel.txStats.values.sortedBy { it.latest.id },
            )
        }
    }
}
