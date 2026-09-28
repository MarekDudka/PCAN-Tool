package com.pcantool.pcanbasic

/** PCAN-Basic `PCAN_BAUD_*` BTR0/BTR1 codes for classic CAN bit rates. */
enum class PcanBaudRate(val code: Int, val label: String) {
    BAUD_1M(0x0014, "1 MBit/s"),
    BAUD_800K(0x0016, "800 kBit/s"),
    BAUD_500K(0x001C, "500 kBit/s"),
    BAUD_250K(0x011C, "250 kBit/s"),
    BAUD_125K(0x031C, "125 kBit/s"),
    BAUD_100K(0x432F, "100 kBit/s"),
    BAUD_95K(0xC34E, "95.238 kBit/s"),
    BAUD_83K(0x852B, "83.333 kBit/s"),
    BAUD_50K(0x472F, "50 kBit/s"),
    BAUD_47K(0x1414, "47.619 kBit/s"),
    BAUD_33K(0x8B2F, "33.333 kBit/s"),
    BAUD_20K(0x532F, "20 kBit/s"),
    BAUD_10K(0x672F, "10 kBit/s"),
    BAUD_5K(0x7F7F, "5 kBit/s"),
    ;

    companion object {
        val default = BAUD_500K
    }
}
