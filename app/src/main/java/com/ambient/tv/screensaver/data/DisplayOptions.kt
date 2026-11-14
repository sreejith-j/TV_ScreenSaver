package com.ambient.tv.screensaver.data

import java.util.Calendar

enum class DisplayMode(val id: String, val displayName: String, val subtitle: String) {
    HORIZONTAL("horizontal", "Minimal Horizontal", "Clean single-line modern time"),
    STACKED("stacked", "Pixel Stacked", "Bold stacked hour and minute"),
    ANALOG("analog", "Minimalist Analog", "Contemporary Bauhaus dial & hands"),
    DOT_MATRIX("dot_matrix", "Cyber Dot Matrix", "Futuristic high-tech digital style");

    companion object {
        fun fromId(id: String): DisplayMode =
            entries.find { it.id == id } ?: HORIZONTAL

        fun next(current: DisplayMode): DisplayMode {
            val idx = (current.ordinal + 1) % entries.size
            return entries[idx]
        }
    }
}

enum class AccentTheme(
    val id: String,
    val displayName: String,
    private val rawColor: Long
) {
    GOOGLE_BLUE("blue", "Google Blue", 0xFF8AB4F8),
    SUNSET_GOLD("gold", "Sunset Gold", 0xFFFFB74D),
    EMERALD_MINT("emerald", "Emerald Mint", 0xFF00E676),
    CYBER_CYAN("cyan", "Cyber Cyan", 0xFF00E5FF),
    LAVENDER_VIOLET("violet", "Lavender Violet", 0xFFB388FF),
    CORAL_PEACH("coral", "Coral Peach", 0xFFFF7043),
    MINIMAL_WHITE("white", "Minimal White", 0xFFFFFFFF),
    CIRCADIAN("circadian", "Dynamic Circadian", 0L);

    fun getColor(): Int {
        if (this == CIRCADIAN) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val hex = when (hour) {
                in 6..11 -> 0xFFFFB74D // Sunrise Gold
                in 12..17 -> 0xFF8AB4F8 // Day Sky Blue
                in 18..21 -> 0xFFFF7043 // Evening Coral
                else -> 0xFF9FA8DA      // Night Moonlight
            }
            return hex.toInt()
        }
        return rawColor.toInt()
    }

    companion object {
        fun fromId(id: String): AccentTheme =
            entries.find { it.id == id } ?: GOOGLE_BLUE

        fun next(current: AccentTheme): AccentTheme {
            val idx = (current.ordinal + 1) % entries.size
            return entries[idx]
        }
    }
}

enum class ParticleMode(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    AUTO("auto", "Auto (Matches Weather)", "Rain, snow, or stars matching live weather"),
    RAIN("rain", "Gentle Rain", "Soothing rainfall streaks"),
    SNOW("snow", "Floating Snow", "Gentle drifting snowflakes"),
    STARS("stars", "Twinkling Stars", "Atmospheric night starfield"),
    SUN_MOTES("sun_motes", "Sun Motes", "Warm glowing ambient dust particles"),
    OFF("off", "Off (Static)", "No particles for pure minimalist display");

    companion object {
        fun fromId(id: String): ParticleMode =
            entries.find { it.id == id } ?: AUTO

        fun next(current: ParticleMode): ParticleMode {
            val idx = (current.ordinal + 1) % entries.size
            return entries[idx]
        }
    }
}
