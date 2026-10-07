package com.example.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class SubjectChipColors(
    val background: Color,
    val text: Color
)

object SubjectColorUtil {

    private val KNOWN_HUES = listOf(
        "phys" to 220f,
        "পদার্থ" to 220f,
        "chem" to 165f,
        "রসায়ন" to 165f,
        "bio" to 105f,
        "জীব" to 105f,
        "math" to 38f,
        "গণিত" to 38f,
        "eng" to 330f,
        "ইংরেজ" to 330f,
        "bang" to 2f,
        "বাংলা" to 2f,
        "ict" to 268f
    )

    fun getHueForSubject(subject: String): Float {
        val lower = subject.lowercase()
        for ((key, hue) in KNOWN_HUES) {
            if (lower.contains(key)) return hue
        }
        // Custom deterministic hash
        var hash = 0
        for (ch in lower) {
            hash = (hash * 31 + ch.code) % 360
        }
        return abs(hash.toFloat())
    }

    fun getColorsForSubject(subject: String, isDark: Boolean): SubjectChipColors {
        val hue = getHueForSubject(subject)

        return if (isDark) {
            // Dark mode: chip-s: 45%, chip-bg-l: 24%, chip-fg-l: 86%
            val bg = hslToColor(hue, 0.45f, 0.24f)
            val fg = hslToColor(hue, 0.60f, 0.86f)
            SubjectChipColors(bg, fg)
        } else {
            // Light mode: chip-s: 70%, chip-bg-l: 92%, chip-fg-l: 26%
            val bg = hslToColor(hue, 0.70f, 0.92f)
            val fg = hslToColor(hue, 0.60f, 0.26f)
            SubjectChipColors(bg, fg)
        }
    }

    private fun hslToColor(h: Float, s: Float, l: Float): Color {
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        var r = 0f
        var g = 0f
        var b = 0f

        when {
            h < 60f -> { r = c; g = x; b = 0f }
            h < 120f -> { r = x; g = c; b = 0f }
            h < 180f -> { r = 0f; g = c; b = x }
            h < 240f -> { r = 0f; g = x; b = c }
            h < 300f -> { r = x; g = 0f; b = c }
            else -> { r = c; g = 0f; b = x }
        }

        return Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f),
            alpha = 1f
        )
    }
}
