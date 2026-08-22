package com.example.kmpincidents.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max

private val chartColors = listOf(
    Color(0xFF1565C0),
    Color(0xFF2E7D32),
    Color(0xFFEF6C00),
    Color(0xFFC62828),
    Color(0xFF6A1B9A),
    Color(0xFF00838F),
    Color(0xFFAD1457),
    Color(0xFF4527A0),
)

@Composable
fun SimpleStatsChart(
    labels: List<String>,
    values: List<Int>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val safeCount = minOf(labels.size, values.size)
    val safeLabels = labels.take(safeCount)
    val safeValues = values.take(safeCount)
    val maxValue = max(1, safeValues.maxOrNull() ?: 1)

    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (safeCount == 0) {
            Text(
                text = "No data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barCount = safeCount
                    val gap = size.width * 0.03f
                    val totalGap = gap * (barCount + 1)
                    val barWidth = ((size.width - totalGap) / barCount).coerceAtLeast(8f)
                    val chartBottom = size.height * 0.88f
                    val chartTop = size.height * 0.08f
                    val chartHeight = chartBottom - chartTop

                    // baseline
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, chartBottom),
                        end = Offset(size.width, chartBottom),
                        strokeWidth = 2f
                    )

                    safeValues.forEachIndexed { index, value ->
                        val barHeight = (value.toFloat() / maxValue.toFloat()) * chartHeight
                        val left = gap + index * (barWidth + gap)
                        val top = chartBottom - barHeight
                        val color = chartColors[index % chartColors.size]

                        drawRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            safeLabels.forEachIndexed { index, label ->
                val value = safeValues.getOrElse(index) { 0 }
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = chartColors[index % chartColors.size]
                )
            }
        }
    }
}
