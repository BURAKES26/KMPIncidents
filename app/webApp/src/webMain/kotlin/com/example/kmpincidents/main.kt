package com.example.kmpincidents

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.kmpincidents.di.dataModule
import com.example.kmpincidents.di.networkModule
import com.example.kmpincidents.di.viewModelModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(networkModule, dataModule, viewModelModule)
    }
    ComposeViewport(viewportContainerId = "composeApplication") {
        App()
    }
}