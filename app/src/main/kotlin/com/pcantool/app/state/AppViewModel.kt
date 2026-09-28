package com.pcantool.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pcantool.core.CanMessage
import com.pcantool.core.CsvExporter
import com.pcantool.core.Direction
import com.pcantool.core.MessageFilter
import com.pcantool.core.MessageStat
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val MAX_TRACE_ROWS = 5000

/**
 * Holds all mutable UI state and owns the PCAN connection. Every mutation of [connection],
 * [recordingSession], [rxStatsTable] or [txStatsTable] happens on [ioDispatcher], a
 * single-threaded dispatcher, so the background read loop and UI-triggered actions
 * (connect/disconnect/send/record/export) never race on that state even though nothing here is
 * explicitly locked.
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

    /** The unified Trace log: RX and TX interleaved in the order they actually happened. */
    val traceMessages = mutableStateListOf<DirectedMessage>()
    val rxStats = mutableStateMapOf<Long, MessageStat>()
    val txStats = mutableStateMapOf<Long, MessageStat>()

    var isRecording by mutableStateOf(false)
        private set
    var recordedCount by mutableStateOf(0)
        private set

    var lastExportError by mutableStateOf<String?>(null)
    var lastSendError by mutableStateOf<String?>(null)

    val periodicMessages = mutableStateListOf<PeriodicMessageEntry>()

    private var connection: PcanConnection? = null
    private var readJob: Job? = null
    private val rxStatsTable = MessageStatsTable()
    private val txStatsTable = MessageStatsTable()
    private val recordingSession = RecordingSession()
    private val periodicJobs = mutableMapOf<PeriodicMessageEntry, Job>()

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
                conn.readFlow().collect { message -> handleReceived(message) }
            } catch (e: CancellationException) {
                // expected when disconnect() cancels this job
            } catch (e: Exception) {
                connectionState = ConnectionState.Error(e.message ?: "Unknown PCAN error")
            } finally {
                connection?.close()
                connection = null
                stopAllPeriodic()
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

    /** Sends one frame immediately; no-ops with [lastSendError] set if not connected. */
    fun sendMessage(id: Long, extended: Boolean, remote: Boolean, data: ByteArray) {
        scope.launch(ioDispatcher) { sendOnce(id, extended, remote, data) }
    }

    /**
     * Adds a new cyclic TX entry, initially disabled — flip it on via [setPeriodicEnabled].
     * Routed through [ioDispatcher] like every other mutation of [periodicJobs] so enabling,
     * disabling and the connect/disconnect cleanup below never race on that map.
     */
    fun addPeriodicMessage(id: Long, extended: Boolean, remote: Boolean, data: ByteArray, intervalMillis: Long) {
        scope.launch(ioDispatcher) {
            periodicMessages.add(PeriodicMessageEntry(id, extended, remote, data, intervalMillis))
        }
    }

    fun removePeriodicMessage(entry: PeriodicMessageEntry) {
        scope.launch(ioDispatcher) {
            periodicJobs.remove(entry)?.cancel()
            entry.enabled = false
            periodicMessages.remove(entry)
        }
    }

    fun setPeriodicEnabled(entry: PeriodicMessageEntry, enabled: Boolean) {
        scope.launch(ioDispatcher) {
            if (enabled == entry.enabled) return@launch
            entry.enabled = enabled
            if (enabled) {
                periodicJobs[entry] = scope.launch(ioDispatcher) {
                    while (isActive) {
                        sendOnce(entry.id, entry.extended, entry.remote, entry.data)
                        delay(entry.intervalMillis)
                    }
                }
            } else {
                periodicJobs.remove(entry)?.cancel()
            }
        }
    }

    /** Called from within [connect]'s own `finally` block, already on [ioDispatcher]. */
    private fun stopAllPeriodic() {
        periodicJobs.values.forEach { it.cancel() }
        periodicJobs.clear()
        periodicMessages.forEach { it.enabled = false }
    }

    /** Must run on [ioDispatcher] — called both for one-shot sends and from each periodic loop tick. */
    private fun sendOnce(id: Long, extended: Boolean, remote: Boolean, data: ByteArray) {
        val conn = connection
        if (conn == null) {
            lastSendError = "Not connected"
            return
        }
        try {
            val sent = conn.write(id, extended, remote, data)
            lastSendError = null
            val stat = txStatsTable.update(sent)
            txStats[stat.key] = stat
            if (filter.matches(sent)) {
                addToTrace(sent, Direction.TX)
            }
        } catch (e: Exception) {
            lastSendError = e.message ?: "Failed to send"
        }
    }

    private fun handleReceived(message: CanMessage) {
        val stat = rxStatsTable.update(message)
        rxStats[stat.key] = stat
        if (filter.matches(message)) {
            addToTrace(message, Direction.RX)
        }
    }

    private fun addToTrace(message: CanMessage, direction: Direction) {
        traceMessages.add(DirectedMessage(message, direction))
        if (traceMessages.size > MAX_TRACE_ROWS) traceMessages.removeAt(0)
        if (recordingSession.record(message, direction) != null) {
            recordedCount = recordingSession.count
        }
    }

    fun clearTrace() {
        traceMessages.clear()
        rxStats.clear()
        txStats.clear()
        rxStatsTable.clear()
        txStatsTable.clear()
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
