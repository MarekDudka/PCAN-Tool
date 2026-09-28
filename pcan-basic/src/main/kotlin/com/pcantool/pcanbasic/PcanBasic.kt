package com.pcantool.pcanbasic

import com.pcantool.pcanbasic.native.PCANBasicLibrary

private const val PARAM_CHANNEL_CONDITION: Byte = 0x0D
private const val CHANNEL_AVAILABLE_BIT: Int = 0x01

/** Queries not tied to a single open connection. */
object PcanBasic {
    /**
     * USB channels PCAN-Basic currently reports as plugged in and free (via
     * `CAN_GetValue(..., PCAN_CHANNEL_CONDITION, ...)`), for populating the channel picker.
     * Returns an empty list (rather than throwing) if the native library isn't installed yet,
     * so the UI can still show the manual channel list.
     */
    fun availableUsbChannels(): List<PcanChannel> {
        val lib = try {
            PCANBasicLibrary.INSTANCE
        } catch (e: UnsatisfiedLinkError) {
            return emptyList()
        }
        return PcanChannel.usbChannels.filter { channel ->
            val buffer = PCANBasicLibrary.newValueBuffer()
            val status = lib.CAN_GetValue(channel.handle.toShort(), PARAM_CHANNEL_CONDITION, buffer, 4)
            status == PcanError.OK && (buffer.getInt(0) and CHANNEL_AVAILABLE_BIT) != 0
        }
    }
}
