package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.Stat

@Composable
actual fun PlatformStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    modifier: Modifier
) {
    val data = mapOf(
        "label" to labels,
        "count" to values
    )

    val fig = letsPlot(data) +
            geomBar(stat = Stat.identity) {
                x = "label"
                y = "count"
            } +
            ggtitle(title)

    PlotPanel(
        figure = fig,
        modifier = modifier
    )
}