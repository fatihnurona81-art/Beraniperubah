package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.data.AppTheme

private val StandardLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    secondary = CyberpunkSecondary,
    tertiary = CyberpunkTertiary,
    background = CyberpunkBackground,
    surface = CyberpunkSurface,
    onBackground = CyberpunkOnBackground,
    onSurface = CyberpunkOnSurface
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    background = OceanBackground,
    surface = OceanSurface,
    onBackground = OceanOnBackground,
    onSurface = OceanOnSurface
)

private val SakuraColorScheme = darkColorScheme(
    primary = SakuraPrimary,
    secondary = SakuraSecondary,
    background = SakuraBackground,
    surface = SakuraSurface,
    onBackground = SakuraOnBackground,
    onSurface = SakuraOnSurface
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> StandardLightColorScheme
        AppTheme.DARK -> StandardDarkColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.OCEAN_BLUE -> OceanColorScheme
        AppTheme.SAKURA_PINK -> SakuraColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
