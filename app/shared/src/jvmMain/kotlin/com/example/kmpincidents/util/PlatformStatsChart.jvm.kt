package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.kmpincidents.ui.SimpleStatsChart

@Composable
actual fun PlatformStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    modifier: Modifier,
) {
    SimpleStatsChart(
        labels = labels,
        values = values,
        title = title,
        modifier = modifier
    )
}
