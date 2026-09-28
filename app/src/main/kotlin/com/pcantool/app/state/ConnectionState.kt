package com.pcantool.app.state

import com.pcantool.pcanbasic.PcanChannel

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val channel: PcanChannel) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
