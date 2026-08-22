package com.example.kmpincidents

import androidx.compose.runtime.Composable
import com.example.kmpincidents.navigation.AppNavigation
import com.example.kmpincidents.navigation.DesktopAppNavigation
import com.example.kmpincidents.ui.theme.IncidentsComposeTheme

@Composable
fun App() {
    IncidentsComposeTheme {
        AppNavigation()
    }
}

/**
 * Desktop entry composition: official-only login and master-detail home navigation.
 * Android/Web/iOS continue to use [App].
 */
@Composable
fun DesktopApp() {
    IncidentsComposeTheme {
        DesktopAppNavigation()
    }
}