package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class RoutineColors(
    val bg: Color,
    val surface: Color,
    val field: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val accent: Color,
    val accentInk: Color,
    val today: Color,
    val heroBg: Color,
    val heroFg: Color,
    val heroMuted: Color,
    val danger: Color
)

val LocalRoutineColors = staticCompositionLocalOf {
    RoutineColors(
        bg = LightBg,
        surface = LightSurface,
        field = LightField,
        ink = LightInk,
        muted = LightMuted,
        line = LightLine,
        accent = LightAccent,
        accentInk = LightAccentInk,
        today = LightToday,
        heroBg = LightHeroBg,
        heroFg = LightHeroFg,
        heroMuted = LightHeroMuted,
        danger = LightDanger
    )
}

val LightRoutineColors = RoutineColors(
    bg = LightBg,
    surface = LightSurface,
    field = LightField,
    ink = LightInk,
    muted = LightMuted,
    line = LightLine,
    accent = LightAccent,
    accentInk = LightAccentInk,
    today = LightToday,
    heroBg = LightHeroBg,
    heroFg = LightHeroFg,
    heroMuted = LightHeroMuted,
    danger = LightDanger
)

val DarkRoutineColors = RoutineColors(
    bg = DarkBg,
    surface = DarkSurface,
    field = DarkField,
    ink = DarkInk,
    muted = DarkMuted,
    line = DarkLine,
    accent = DarkAccent,
    accentInk = DarkAccentInk,
    today = DarkToday,
    heroBg = DarkHeroBg,
    heroFg = DarkHeroFg,
    heroMuted = DarkHeroMuted,
    danger = DarkDanger
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightAccentInk,
    primaryContainer = LightToday,
    onPrimaryContainer = LightAccent,
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightField,
    onSurfaceVariant = LightMuted,
    outline = LightLine,
    error = LightDanger
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkAccentInk,
    primaryContainer = DarkToday,
    onPrimaryContainer = DarkAccent,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkField,
    onSurfaceVariant = DarkMuted,
    outline = DarkLine,
    error = DarkDanger
)

@Composable
fun TuitionRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val routineColors = if (darkTheme) DarkRoutineColors else LightRoutineColors

    CompositionLocalProvider(LocalRoutineColors provides routineColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object RoutineTheme {
    val colors: RoutineColors
        @Composable
        get() = LocalRoutineColors.current
}
