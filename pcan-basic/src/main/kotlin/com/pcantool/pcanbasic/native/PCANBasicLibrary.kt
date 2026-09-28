package com.pcantool.pcanbasic.native

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Pointer

/**
 * Direct mapping of the subset of PCAN-Basic's C API (PCANBasic.h) this tool needs, for classic
 * CAN (no CAN FD). Calling convention is the platform default, which is correct on 64-bit
 * Windows/Linux where cdecl and stdcall are identical; a 32-bit Windows build would need a
 * `StdCallLibrary` mapping instead.
 */
interface PCANBasicLibrary : Library {
    fun CAN_Initialize(channel: Short, btr0Btr1: Short, hwType: Byte, ioPort: Int, interrupt: Short): Int

    fun CAN_Uninitialize(channel: Short): Int

    fun CAN_Reset(channel: Short): Int

    fun CAN_GetStatus(channel: Short): Int

    fun CAN_Read(channel: Short, messageBuffer: TPCANMsg, timestampBuffer: TPCANTimestamp?): Int

    fun CAN_Write(channel: Short, messageBuffer: TPCANMsg): Int

    fun CAN_FilterMessages(channel: Short, fromId: Int, toId: Int, mode: Byte): Int

    fun CAN_GetValue(channel: Short, parameter: Byte, buffer: Pointer, bufferLength: Int): Int

    fun CAN_SetValue(channel: Short, parameter: Byte, buffer: Pointer, bufferLength: Int): Int

    fun CAN_GetErrorText(error: Int, language: Short, buffer: Pointer): Int

    companion object {
        /** Lazily loads PCANBasic.dll (Windows) / libpcanbasic.so (Linux) from the system library path. */
        val INSTANCE: PCANBasicLibrary by lazy {
            val libraryName = if (Platform.isWindows()) "PCANBasic" else "pcanbasic"
            Native.load(libraryName, PCANBasicLibrary::class.java)
        }

        fun newErrorTextBuffer(): Memory = Memory(256)

        fun newValueBuffer(): Memory = Memory(4)
    }
}
