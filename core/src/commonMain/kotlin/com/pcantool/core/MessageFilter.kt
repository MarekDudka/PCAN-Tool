package com.pcantool.core

/**
 * A display/recording filter for [CanMessage]s, modeled after PCAN-View's message filter:
 * an inclusive CAN-id range plus which frame kinds to let through.
 */
data class MessageFilter(
    val fromId: Long = 0,
    val toId: Long = 0x1FFFFFFF,
    val allowStandard: Boolean = true,
    val allowExtended: Boolean = true,
    val allowData: Boolean = true,
    val allowRemote: Boolean = true,
    /** Free-text search over hex id and hex data, case-insensitive. Blank disables it. */
    val searchText: String = "",
) {
    fun matches(message: CanMessage): Boolean {
        if (message.id < fromId || message.id > toId) return false
        if (message.extended && !allowExtended) return false
        if (!message.extended && !allowStandard) return false
        if (message.remote && !allowRemote) return false
        if (!message.remote && !allowData) return false
        if (searchText.isNotBlank()) {
            val needle = searchText.trim().uppercase()
            val haystack = message.idHex() + " " + message.dataHex()
            if (!haystack.contains(needle)) return false
        }
        return true
    }

    companion object {
        val ACCEPT_ALL = MessageFilter()
    }
}
