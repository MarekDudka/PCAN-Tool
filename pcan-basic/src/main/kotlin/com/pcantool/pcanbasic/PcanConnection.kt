package com.pcantool.pcanbasic

import com.pcantool.core.CanMessage
import com.pcantool.pcanbasic.native.PCANBasicLibrary
import com.pcantool.pcanbasic.native.TPCANMsg
import com.pcantool.pcanbasic.native.TPCANTimestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val MSGTYPE_STANDARD: Int = 0x00
private const val MSGTYPE_RTR: Int = 0x01
private const val MSGTYPE_EXTENDED: Int = 0x02

private const val MODE_STANDARD: Byte = 0x00
private const val MODE_EXTENDED: Byte = 0x01

private const val PARAM_MESSAGE_FILTER: Byte = 0x04
private const val FILTER_OPEN: Int = 0x01

/** An initialized PCAN-Basic channel. Obtain one via [PcanConnection.open]; call [close] when done. */
class PcanConnection internal constructor(private val channel: PcanChannel) : AutoCloseable {
    private val handle: Short = channel.handle.toShort()
    private val lib get() = PCANBasicLibrary.INSTANCE

    fun write(id: Long, extended: Boolean, remote: Boolean, data: ByteArray) {
        require(data.size <= 8) { "Classic CAN frames carry at most 8 data bytes" }
        val msg = TPCANMsg()
        msg.id = id.toInt()
        var type = if (extended) MSGTYPE_EXTENDED else MSGTYPE_STANDARD
        if (remote) type = type or MSGTYPE_RTR
        msg.msgType = type.toByte()
        msg.len = data.size.toByte()
        msg.data = ByteArray(8).also { data.copyInto(it) }
        val status = PcanStatus(lib.CAN_Write(handle, msg))
        if (!status.isOk) throw PcanException(status)
    }

    /** Opens the hardware-level acceptance range for one frame kind (standard or extended ids). */
    fun setRangeFilter(fromId: Long, toId: Long, extended: Boolean) {
        val mode = if (extended) MODE_EXTENDED else MODE_STANDARD
        val status = PcanStatus(lib.CAN_FilterMessages(handle, fromId.toInt(), toId.toInt(), mode))
        if (!status.isOk) throw PcanException(status)
    }

    /** Re-opens the hardware filter to accept every id; PCAN-Tool's own [com.pcantool.core.MessageFilter] does the rest. */
    fun resetFilter() {
        val buffer = PCANBasicLibrary.newValueBuffer()
        buffer.setInt(0, FILTER_OPEN)
        val status = PcanStatus(lib.CAN_SetValue(handle, PARAM_MESSAGE_FILTER, buffer, 4))
        if (!status.isOk) throw PcanException(status)
    }

    fun status(): PcanStatus = PcanStatus(lib.CAN_GetStatus(handle))

    override fun close() {
        lib.CAN_Uninitialize(handle)
    }

    /**
     * Emits messages as they arrive. Implemented by polling `CAN_Read` rather than wiring up
     * `PCAN_RECEIVE_EVENT`, since that event handle is a native `HANDLE` on Windows and a file
     * descriptor on Linux and would need two separate native wait mechanisms; polling is simpler
     * and portable, at the cost of up to [pollIntervalMillis] of extra latency per message.
     */
    fun readFlow(pollIntervalMillis: Long = 1): Flow<CanMessage> = flow {
        val msg = TPCANMsg()
        val timestamp = TPCANTimestamp()
        while (true) {
            val status = PcanStatus(lib.CAN_Read(handle, msg, timestamp))
            if (status.isReceiveQueueEmpty) {
                delay(pollIntervalMillis)
            } else if (status.isOk) {
                emit(msg.toCanMessage(timestamp))
            } else {
                throw PcanException(status)
            }
        }
    }

    companion object {
        fun open(channel: PcanChannel, baudRate: PcanBaudRate): PcanConnection {
            val status = try {
                PcanStatus(
                    PCANBasicLibrary.INSTANCE.CAN_Initialize(
                        channel.handle.toShort(),
                        baudRate.code.toShort(),
                        0,
                        0,
                        0,
                    ),
                )
            } catch (e: UnsatisfiedLinkError) {
                throw PcanLibraryNotFoundException(e)
            }
            if (!status.isOk) throw PcanException(status)
            return PcanConnection(channel)
        }
    }
}

private fun TPCANMsg.toCanMessage(timestamp: TPCANTimestamp): CanMessage {
    val type = msgType.toInt() and 0xFF
    val length = (len.toInt() and 0xFF).coerceIn(0, 8)
    return CanMessage(
        id = id.toLong() and 0xFFFFFFFFL,
        extended = (type and MSGTYPE_EXTENDED) != 0,
        remote = (type and MSGTYPE_RTR) != 0,
        data = data.copyOf(length),
        timestampMicros = timestamp.toMicros(),
    )
}
