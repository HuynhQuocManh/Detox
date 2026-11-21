package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MonochromeDarkScheme = darkColorScheme(
    primary = InkOnBackground,
    onPrimary = InkBackground,
    primaryContainer = InkSurfaceHighest,
    onPrimaryContainer = InkOnBackground,
    secondary = InkOnSurfaceVariant,
    onSecondary = InkBackground,
    secondaryContainer = InkSurfaceElevated,
    onSecondaryContainer = InkOnBackground,
    tertiary = InkOnSurfaceVariant,
    onTertiary = InkBackground,
    background = InkBackground,
    onBackground = InkOnBackground,
    surface = InkSurface,
    onSurface = InkOnBackground,
    surfaceVariant = InkSurfaceElevated,
    onSurfaceVariant = InkOnSurfaceVariant,
    surfaceContainerLowest = InkBackground,
    surfaceContainerLow = InkSurface,
    surfaceContainer = InkSurfaceElevated,
    surfaceContainerHigh = InkSurfaceHighest,
    surfaceContainerHighest = InkSurfaceHighest,
    outline = InkBorderStrong,
    outlineVariant = InkBorder,
    inverseSurface = InkOnBackground,
    inverseOnSurface = InkBackground,
    inversePrimary = InkBackground,
    error = InkOnBackground,
    onError = InkBackground
)

private val MonochromeLightScheme = lightColorScheme(
    primary = PaperOnBackground,
    onPrimary = PaperSurface,
    primaryContainer = PaperSurfaceHighest,
    onPrimaryContainer = PaperOnBackground,
    secondary = PaperOnSurfaceVariant,
    onSecondary = PaperSurface,
    secondaryContainer = PaperSurfaceElevated,
    onSecondaryContainer = PaperOnBackground,
    tertiary = PaperOnSurfaceVariant,
    onTertiary = PaperSurface,
    background = PaperBackground,
    onBackground = PaperOnBackground,
    surface = PaperSurface,
    onSurface = PaperOnBackground,
    surfaceVariant = PaperSurfaceElevated,
    onSurfaceVariant = PaperOnSurfaceVariant,
    surfaceContainerLowest = PaperSurface,
    surfaceContainerLow = PaperBackground,
    surfaceContainer = PaperSurfaceElevated,
    surfaceContainerHigh = PaperSurfaceHighest,
    surfaceContainerHighest = PaperSurfaceHighest,
    outline = PaperBorderStrong,
    outlineVariant = PaperBorder,
    inverseSurface = PaperOnBackground,
    inverseOnSurface = PaperSurface,
    inversePrimary = PaperSurface,
    error = PaperOnBackground,
    onError = PaperSurface
)

val TinhDetoxShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TinhDetoxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkScheme else MonochromeLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TinhDetoxTypography,
        shapes = TinhDetoxShapes,
        content = content
    )
}
