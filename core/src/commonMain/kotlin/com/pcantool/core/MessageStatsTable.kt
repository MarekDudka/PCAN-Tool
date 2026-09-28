package com.pcantool.core

data class MessageStat(
    val key: Long,
    val latest: CanMessage,
    val count: Long,
    val cycleTimeMicros: Long?,
)

/**
 * Tracks one row per distinct CAN id, as PCAN-View's "Receive" overview does: latest payload,
 * total count and the time between the last two occurrences of that id.
 */
class MessageStatsTable {
    private val rows = LinkedHashMap<Long, MessageStat>()

    fun update(message: CanMessage): MessageStat {
        val key = statKey(message)
        val previous = rows[key]
        val cycleTime = previous?.let { message.timestampMicros - it.latest.timestampMicros }
        val updated = MessageStat(
            key = key,
            latest = message,
            count = (previous?.count ?: 0) + 1,
            cycleTimeMicros = cycleTime ?: previous?.cycleTimeMicros,
        )
        rows[key] = updated
        return updated
    }

    fun values(): List<MessageStat> = rows.values.sortedBy { it.latest.id }

    fun clear() = rows.clear()

    private fun statKey(message: CanMessage): Long =
        if (message.extended) message.id or (1L shl 32) else message.id
}
