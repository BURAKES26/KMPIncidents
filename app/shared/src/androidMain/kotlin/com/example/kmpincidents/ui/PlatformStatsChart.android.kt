package com.example.kmpincidents.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.theme

@Composable
actual fun PlatformStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    modifier: Modifier
) {
    val data = mapOf(
        "Label" to labels,
        "Count" to values
    )

    val plot = letsPlot(data) +
            geomBar(stat = Stat.identity) { x = "Label"; y = "Count"; fill = "Label" } +
            ggtitle(title) +
            theme(
                axisTitle = elementBlank(),
                axisText = elementBlank(),
                axisTicks = elementBlank(),
                axisLine = elementBlank(),
                panelGrid = elementBlank()
            )

    PlotPanel(figure = plot, modifier = modifier, computationMessagesHandler = { })
}