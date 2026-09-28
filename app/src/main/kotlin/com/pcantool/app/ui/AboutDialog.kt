package com.pcantool.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.awt.Desktop
import java.net.URI

private const val REPO_URL = "https://github.com/MarekDudka/PCAN-Tool"
private const val CHANGELOG_URL = "https://github.com/MarekDudka/PCAN-Tool/blob/master/CHANGELOG.md"
private const val AUTHOR_URL = "https://github.com/MarekDudka"
private const val EULA_URL = "https://www.peak-system.com/support/eula/"

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(24.dp).widthIn(min = 360.dp)) {
                Text("PCAN-Tool", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Viewer, filter, recorder and CSV exporter for PEAK-System PCAN-Basic adapters.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                LinkRow("Author", "Marek Dudka", AUTHOR_URL)
                LinkRow("Source", "github.com/MarekDudka/PCAN-Tool", REPO_URL)
                LinkRow("Changelog", "View release notes", CHANGELOG_URL)
                LinkRow("PEAK-System EULA", "Terms for the PCAN-Basic library", EULA_URL)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun LinkRow(label: String, linkText: String, url: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.widthIn(min = 120.dp))
        TextButton(onClick = { openUrl(url) }) { Text(linkText) }
    }
}

private fun openUrl(url: String) {
    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    } catch (e: Exception) {
        // No default browser available in this environment; nothing sensible to do but ignore.
    }
}
