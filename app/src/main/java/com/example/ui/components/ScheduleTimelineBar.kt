package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.model.DetoxScheduleStatus

@Composable
fun ScheduleTimelineBar(
    status: DetoxScheduleStatus,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val activeFillColor = MaterialTheme.colorScheme.onBackground
    val borderColor = MaterialTheme.colorScheme.outline
    val markerBgColor = MaterialTheme.colorScheme.background
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
        ) {
            val totalMinutes = 1440f
            val barHeight = 8.dp.toPx()
            val barTop = (size.height - barHeight) / 2f
            val corner = CornerRadius(barHeight / 2f, barHeight / 2f)

            // Full 24h base track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, barTop),
                size = Size(size.width, barHeight),
                cornerRadius = corner
            )

            // Highlight scheduled detox interval(s)
            if (status.scheduleEnabled) {
                val startFrac = (status.startMinutes / totalMinutes).coerceIn(0f, 1f)
                val endFrac = (status.endMinutes / totalMinutes).coerceIn(0f, 1f)

                if (status.startMinutes == status.endMinutes) {
                    drawRoundRect(
                        color = activeFillColor,
                        topLeft = Offset(0f, barTop),
                        size = Size(size.width, barHeight),
                        cornerRadius = corner
                    )
                } else if (status.startMinutes < status.endMinutes) {
                    val left = startFrac * size.width
                    val width = ((endFrac - startFrac) * size.width).coerceAtLeast(2.dp.toPx())
                    drawRoundRect(
                        color = activeFillColor,
                        topLeft = Offset(left, barTop),
                        size = Size(width, barHeight),
                        cornerRadius = corner
                    )
                } else {
                    // Overnight: [start -> 24:00] and [00:00 -> end]
                    val rightStart = startFrac * size.width
                    val rightWidth = (size.width - rightStart).coerceAtLeast(2.dp.toPx())
                    drawRoundRect(
                        color = activeFillColor,
                        topLeft = Offset(rightStart, barTop),
                        size = Size(rightWidth, barHeight),
                        cornerRadius = corner
                    )
                    val leftWidth = (endFrac * size.width).coerceAtLeast(2.dp.toPx())
                    drawRoundRect(
                        color = activeFillColor,
                        topLeft = Offset(0f, barTop),
                        size = Size(leftWidth, barHeight),
                        cornerRadius = corner
                    )
                }
            }

            // Current time marker
            val nowFrac = (status.currentMinutes / totalMinutes).coerceIn(0f, 1f)
            val markerX = (nowFrac * size.width).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx())
            val centerY = size.height / 2f

            drawCircle(
                color = markerBgColor,
                radius = 7.dp.toPx(),
                center = Offset(markerX, centerY)
            )
            drawCircle(
                color = activeFillColor,
                radius = 5.dp.toPx(),
                center = Offset(markerX, centerY)
            )
            drawCircle(
                color = borderColor,
                radius = 2.dp.toPx(),
                center = Offset(markerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { tick ->
                Text(
                    text = tick,
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedTextColor
                )
            }
        }
    }
}
