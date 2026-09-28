package com.pcantool.pcanbasic

/** PCAN-Basic channel handles (`PCAN_*BUS*` constants from PCANBasic.h) for the buses PCAN-Tool exposes. */
enum class PcanChannel(val handle: Int, val label: String) {
    USB1(0x51, "PCAN-USB 1"),
    USB2(0x52, "PCAN-USB 2"),
    USB3(0x53, "PCAN-USB 3"),
    USB4(0x54, "PCAN-USB 4"),
    USB5(0x55, "PCAN-USB 5"),
    USB6(0x56, "PCAN-USB 6"),
    USB7(0x57, "PCAN-USB 7"),
    USB8(0x58, "PCAN-USB 8"),
    PCI1(0x41, "PCAN-PCI 1"),
    PCI2(0x42, "PCAN-PCI 2"),
    PCI3(0x43, "PCAN-PCI 3"),
    PCI4(0x44, "PCAN-PCI 4"),
    ;

    companion object {
        val usbChannels: List<PcanChannel> = entries.filter { it.name.startsWith("USB") }

        fun fromHandle(handle: Int): PcanChannel? = entries.find { it.handle == handle }
    }
}
