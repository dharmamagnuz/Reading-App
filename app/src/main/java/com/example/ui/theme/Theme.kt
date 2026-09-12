package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TutorColorScheme = lightColorScheme(
    primary = TutorPrimary,
    onPrimary = TutorOnPrimary,
    primaryContainer = TutorPrimaryContainer,
    onPrimaryContainer = TutorOnPrimaryContainer,
    secondary = TutorSecondary,
    onSecondary = TutorOnSecondary,
    secondaryContainer = TutorSecondaryContainer,
    onSecondaryContainer = TutorOnSecondaryContainer,
    tertiary = TutorTertiary,
    onTertiary = TutorOnTertiary,
    tertiaryContainer = TutorTertiaryContainer,
    onTertiaryContainer = TutorOnTertiaryContainer,
    background = TutorBackground,
    onBackground = TutorOnBackground,
    surface = TutorSurface,
    onSurface = TutorOnSurface,
    surfaceVariant = TutorSurfaceVariant,
    onSurfaceVariant = TutorOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent, readable brand colors for children
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TutorColorScheme,
        typography = Typography,
        content = content
    )
}
