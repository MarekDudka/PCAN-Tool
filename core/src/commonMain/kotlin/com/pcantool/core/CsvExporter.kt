package com.pcantool.core

/** Renders recorded entries as CSV text (platform-agnostic; callers handle the actual file I/O). */
object CsvExporter {
    private const val HEADER = "Sequence,TimeOffsetMs,Direction,Type,ID,DLC,Data"

    fun toCsv(entries: List<RecordedEntry>): String = buildString {
        append(HEADER).append('\n')
        for (entry in entries) {
            append(entry.sequence).append(',')
            append(formatMillis(entry.relativeMicros)).append(',')
            append(entry.direction.name).append(',')
            append(frameType(entry.message)).append(',')
            append(entry.message.idHex()).append(',')
            append(entry.message.dlc).append(',')
            append(entry.message.dataHex())
            append('\n')
        }
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
}
