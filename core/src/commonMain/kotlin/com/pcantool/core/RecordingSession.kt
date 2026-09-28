package com.pcantool.core

enum class Direction { RX, TX }

data class RecordedEntry(
    val sequence: Int,
    val message: CanMessage,
    /** Microseconds elapsed since the first message of this recording. */
    val relativeMicros: Long,
    val direction: Direction = Direction.RX,
)

/**
 * Accumulates [CanMessage]s captured while recording is active. Not thread-safe by itself;
 * this app only ever feeds it from a single reader loop, so no extra locking is needed.
 */
class RecordingSession {
    private val _entries = mutableListOf<RecordedEntry>()
    private var recording = false
    private var startMicros: Long? = null

    val entries: List<RecordedEntry> get() = _entries
    val isRecording: Boolean get() = recording
    val count: Int get() = _entries.size

    fun start() {
        recording = true
        startMicros = null
        _entries.clear()
    }

    fun stop() {
        recording = false
    }

    fun clear() {
        _entries.clear()
        startMicros = null
    }

    /** Feeds a message into the session when recording is active; arms the relative clock on the first entry. */
    fun record(message: CanMessage, direction: Direction = Direction.RX): RecordedEntry? {
        if (!recording) return null
        val base = startMicros ?: message.timestampMicros.also { startMicros = it }
        val entry = RecordedEntry(
            sequence = _entries.size + 1,
            message = message,
            relativeMicros = message.timestampMicros - base,
            direction = direction,
        )
        _entries.add(entry)
        return entry
    }
}
