package com.pcantool.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pcantool.core.CanMessage
import com.pcantool.core.CsvExporter
import com.pcantool.core.MessageFilter
import com.pcantool.core.MessageStatsTable
import com.pcantool.core.RecordingSession
import com.pcantool.pcanbasic.PcanBaudRate
import com.pcantool.pcanbasic.PcanBasic
import com.pcantool.pcanbasic.PcanChannel
import com.pcantool.pcanbasic.PcanConnection
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val MAX_TRACE_ROWS = 5000

/**
 * Holds all mutable UI state and owns the PCAN connection. Every mutation of [connection],
 * [recordingSession] or [statsTable] happens on [ioDispatcher], a single-threaded dispatcher, so
 * the background read loop and UI-triggered actions (connect/disconnect/record/export) never
 * race on that state even though nothing here is explicitly locked.
 */
class AppViewModel(private val scope: CoroutineScope) {
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(1)

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Disconnected)
        private set
    var selectedChannel by mutableStateOf(PcanChannel.USB1)
    var selectedBaudRate by mutableStateOf(PcanBaudRate.default)
    var availableChannels by mutableStateOf<List<PcanChannel>>(emptyList())
        private set
    var filter by mutableStateOf(MessageFilter.ACCEPT_ALL)

    val traceMessages = mutableStateListOf<CanMessage>()
    val stats = mutableStateMapOf<Long, com.pcantool.core.MessageStat>()

    var isRecording by mutableStateOf(false)
        private set
    var recordedCount by mutableStateOf(0)
        private set

    var lastExportError by mutableStateOf<String?>(null)

    private var connection: PcanConnection? = null
    private var readJob: Job? = null
    private val statsTable = MessageStatsTable()
    private val recordingSession = RecordingSession()

    fun refreshAvailableChannels() {
        scope.launch(ioDispatcher) {
            availableChannels = PcanBasic.availableUsbChannels()
        }
    }

    fun connect() {
        if (connectionState is ConnectionState.Connected || connectionState is ConnectionState.Connecting) return
        connectionState = ConnectionState.Connecting
        readJob = scope.launch(ioDispatcher) {
            try {
                val conn = PcanConnection.open(selectedChannel, selectedBaudRate)
                connection = conn
                connectionState = ConnectionState.Connected(selectedChannel)
                conn.readFlow().collect { message -> handleIncoming(message) }
            } catch (e: CancellationException) {
                // expected when disconnect() cancels this job
            } catch (e: Exception) {
                connectionState = ConnectionState.Error(e.message ?: "Unknown PCAN error")
            } finally {
                connection?.close()
                connection = null
                if (connectionState !is ConnectionState.Error) {
                    connectionState = ConnectionState.Disconnected
                }
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
    }

    private fun handleIncoming(message: CanMessage) {
        val stat = statsTable.update(message)
        stats[stat.key] = stat
        if (filter.matches(message)) {
            traceMessages.add(message)
            if (traceMessages.size > MAX_TRACE_ROWS) traceMessages.removeAt(0)
            if (recordingSession.record(message) != null) {
                recordedCount = recordingSession.count
            }
        }
    }

    fun clearTrace() {
        traceMessages.clear()
        stats.clear()
        statsTable.clear()
    }

    fun startRecording() {
        scope.launch(ioDispatcher) {
            recordingSession.start()
            recordedCount = 0
            isRecording = true
        }
    }

    fun stopRecording() {
        scope.launch(ioDispatcher) {
            recordingSession.stop()
            isRecording = false
        }
    }

    fun clearRecording() {
        scope.launch(ioDispatcher) {
            recordingSession.clear()
            recordedCount = 0
        }
    }

    fun exportRecordingToCsv(file: File) {
        scope.launch(ioDispatcher) {
            try {
                file.writeText(CsvExporter.toCsv(recordingSession.entries))
                lastExportError = null
            } catch (e: Exception) {
                lastExportError = e.message ?: "Failed to write CSV file"
            }
        }
    }
}
