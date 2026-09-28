package com.pcantool.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.pcantool.app.state.AppViewModel
import com.pcantool.app.ui.AboutDialog
import com.pcantool.app.ui.AppScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PCAN-Tool",
        state = WindowState(width = 1600.dp, height = 850.dp),
    ) {
        var showAbout by remember { mutableStateOf(false) }

        MenuBar {
            Menu("File") {
                Item("Exit") { exitApplication() }
            }
            Menu("Help") {
                Item("About PCAN-Tool…") { showAbout = true }
            }
        }

        MaterialTheme {
            val scope = rememberCoroutineScope()
            val viewModel = remember { AppViewModel(scope) }
            LaunchedEffect(Unit) { viewModel.refreshAvailableChannels() }
            AppScreen(viewModel)

            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }
    }
}
