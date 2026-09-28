package com.pcantool.pcanbasic.native

import com.sun.jna.Structure

/**
 * Mirrors PCANBasic.h's `TPCANTimestamp`:
 * ```
 * typedef struct tagTPCANTimestamp {
 *     DWORD millis;
 *     WORD  millis_overflow;
 *     WORD  micros;
 * } TPCANTimestamp;
 * ```
 */
@Structure.FieldOrder("millis", "millisOverflow", "micros")
open class TPCANTimestamp : Structure() {
    @JvmField var millis: Int = 0
    @JvmField var millisOverflow: Short = 0
    @JvmField var micros: Short = 0

    class ByReference : TPCANTimestamp(), Structure.ByReference

    /** Total elapsed time since channel initialization, in microseconds, with unsigned rollover handled. */
    fun toMicros(): Long {
        val millisUnsigned = millis.toLong() and 0xFFFFFFFFL
        val overflowUnsigned = millisOverflow.toInt() and 0xFFFF
        val totalMillis = millisUnsigned + overflowUnsigned.toLong() * 0x100000000L
        return totalMillis * 1000L + (micros.toInt() and 0xFFFF)
    }
}
