package com.example.kmpincidents

import androidx.compose.runtime.Composable
import com.example.kmpincidents.navigation.AppNavigation
import com.example.kmpincidents.ui.theme.IncidentsComposeTheme

@Composable
fun App() {
    IncidentsComposeTheme {
        AppNavigation()
    }
}