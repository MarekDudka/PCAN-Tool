package com.pcantool.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** A configured cyclic TX message. Identity-based (no equals/hashCode override) so it can key a job map. */
class PeriodicMessageEntry(
    val id: Long,
    val extended: Boolean,
    val remote: Boolean,
    val data: ByteArray,
    val intervalMillis: Long,
) {
    /** Mutated only via [AppViewModel.setPeriodicEnabled], which also starts/stops the send loop. */
    var enabled: Boolean by mutableStateOf(false)
}
