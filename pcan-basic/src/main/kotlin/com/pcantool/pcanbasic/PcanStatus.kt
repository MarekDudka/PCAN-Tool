package com.pcantool.pcanbasic

import com.pcantool.pcanbasic.native.PCANBasicLibrary

/** Well-known `PCAN_ERROR_*` bits from PCANBasic.h that this tool needs to branch on. */
object PcanError {
    const val OK: Int = 0x00000
    const val XMTFULL: Int = 0x00001
    const val OVERRUN: Int = 0x00002
    const val BUSLIGHT: Int = 0x00004
    const val BUSHEAVY: Int = 0x00008
    const val BUSOFF: Int = 0x00010
    const val QRCVEMPTY: Int = 0x00020
    const val QOVERRUN: Int = 0x00040
    const val QXMTFULL: Int = 0x00080
    const val NODRIVER: Int = 0x00200
    const val ILLHANDLE: Int = 0x01C00
    const val ILLPARAMTYPE: Int = 0x04000
    const val ILLPARAMVAL: Int = 0x08000
}

/** Wraps a raw `TPCANStatus` code returned by PCAN-Basic calls. */
@JvmInline
value class PcanStatus(val code: Int) {
    val isOk: Boolean get() = code == PcanError.OK
    val isReceiveQueueEmpty: Boolean get() = (code and PcanError.QRCVEMPTY) != 0
    val isBusError: Boolean get() = (code and (PcanError.BUSLIGHT or PcanError.BUSHEAVY or PcanError.BUSOFF)) != 0

    /** Human-readable text via `CAN_GetErrorText` (English), falling back to the raw hex code. */
    fun describe(): String = try {
        val buffer = PCANBasicLibrary.newErrorTextBuffer()
        val result = PCANBasicLibrary.INSTANCE.CAN_GetErrorText(code, 0, buffer)
        if (result == PcanError.OK) buffer.getString(0) else "PCAN error 0x${code.toString(16)}"
    } catch (e: UnsatisfiedLinkError) {
        "PCAN error 0x${code.toString(16)}"
    }
}

class PcanException(val status: PcanStatus) : Exception(status.describe())
