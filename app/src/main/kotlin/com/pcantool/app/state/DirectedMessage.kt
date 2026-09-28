package com.pcantool.app.state

import com.pcantool.core.CanMessage
import com.pcantool.core.Direction

/** A message as it appears in the unified Trace log, tagged with which way it went. */
data class DirectedMessage(val message: CanMessage, val direction: Direction)
