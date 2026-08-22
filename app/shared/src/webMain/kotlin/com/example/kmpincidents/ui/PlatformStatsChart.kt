package com.example.kmpincidents.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    columnName: String,
    modifier: Modifier,
) {
    SimpleStatsChart(
        labels = labels,
        values = values,
        title = title,
        modifier = modifier
    )
}
