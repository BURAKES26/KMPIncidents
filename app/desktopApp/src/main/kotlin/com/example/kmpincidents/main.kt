package com.example.kmpincidents

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.kmpincidents.di.dataModule
import com.example.kmpincidents.di.networkModule
import com.example.kmpincidents.di.viewModelModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(networkModule, dataModule, viewModelModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMPIncidents — Desktop",
            state = rememberWindowState(
                width = 1280.dp,
                height = 800.dp,
                placement = WindowPlacement.Maximized,
            ),
        ) {
            DesktopApp()
        }
    }
}