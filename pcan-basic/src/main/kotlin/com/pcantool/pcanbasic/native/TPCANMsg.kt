package com.pcantool.pcanbasic.native

import com.sun.jna.Structure

/**
 * Mirrors PCANBasic.h's `TPCANMsg`:
 * ```
 * typedef struct tagTPCANMsg {
 *     DWORD ID;
 *     BYTE  MSGTYPE;
 *     BYTE  LEN;
 *     BYTE  DATA[8];
 * } TPCANMsg;
 * ```
 * Field order and types must match the C layout exactly; JNA derives native offsets/padding from them.
 */
@Structure.FieldOrder("id", "msgType", "len", "data")
open class TPCANMsg : Structure() {
    @JvmField var id: Int = 0
    @JvmField var msgType: Byte = 0
    @JvmField var len: Byte = 0
    @JvmField var data: ByteArray = ByteArray(8)

    class ByReference : TPCANMsg(), Structure.ByReference
}
