package com.pcantool.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Parses space-separated hex byte pairs (e.g. "01 02 FF") into bytes; empty input yields an empty array. */
internal fun parseHexBytes(text: String): ByteArray? {
    val tokens = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return ByteArray(0)
    val bytes = mutableListOf<Byte>()
    for (token in tokens) {
        val value = token.toIntOrNull(16) ?: return null
        if (value !in 0..255) return null
        bytes.add(value.toByte())
    }
    return bytes.toByteArray()
}

internal fun ByteArray.toSpacedHex(): String = joinToString(" ") { b -> (b.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0') }
