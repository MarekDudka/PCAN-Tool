package com.pcantool.core

/**
 * A single classic CAN frame as received from (or written to) a PCAN channel.
 *
 * @param id 11-bit (standard) or 29-bit (extended) arbitration id.
 * @param extended true for a 29-bit CAN 2.0B identifier, false for an 11-bit CAN 2.0A identifier.
 * @param remote true for a Remote Transmission Request frame (no payload).
 * @param data up to 8 payload bytes; empty for remote frames.
 * @param timestampMicros microseconds since the channel was initialized (from PCAN-Basic's TPCANTimestamp).
 */
data class CanMessage(
    val id: Long,
    val extended: Boolean,
    val remote: Boolean,
    val data: ByteArray,
    val timestampMicros: Long,
) {
    val dlc: Int get() = data.size

    fun idHex(): String = id.toString(16).uppercase().padStart(if (extended) 8 else 3, '0')

    fun dataHex(): String = data.joinToString(" ") { b -> (b.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0') }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanMessage) return false
        return id == other.id &&
            extended == other.extended &&
            remote == other.remote &&
            data.contentEquals(other.data) &&
            timestampMicros == other.timestampMicros
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + extended.hashCode()
        result = 31 * result + remote.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + timestampMicros.hashCode()
        return result
    }
}
