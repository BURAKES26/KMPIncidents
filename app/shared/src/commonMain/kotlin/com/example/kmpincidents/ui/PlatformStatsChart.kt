package com.example.kmpincidents.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    modifier: Modifier = Modifier
)