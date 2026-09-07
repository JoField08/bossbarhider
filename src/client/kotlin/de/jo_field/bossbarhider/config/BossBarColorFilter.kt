package de.jo_field.bossbarhider.config

import net.minecraft.world.BossEvent

enum class BossBarColorFilter(val displayName: String, val vanilla: BossEvent.BossBarColor?) {
    ANY("Any color", null),
    PINK("Pink", BossEvent.BossBarColor.PINK),
    BLUE("Blue", BossEvent.BossBarColor.BLUE),
    RED("Red", BossEvent.BossBarColor.RED),
    GREEN("Green", BossEvent.BossBarColor.GREEN),
    YELLOW("Yellow", BossEvent.BossBarColor.YELLOW),
    PURPLE("Purple", BossEvent.BossBarColor.PURPLE),
    WHITE("White", BossEvent.BossBarColor.WHITE);

    fun next(): BossBarColorFilter = entries[(ordinal + 1) % entries.size]

    fun matches(color: BossEvent.BossBarColor): Boolean = vanilla == null || vanilla == color

    /** ARGB swatch used to draw a small color chip next to the selector in the UI. */
    val swatch: Int
        get() = when (this) {
            ANY -> 0xFF808080.toInt()
            PINK -> 0xFFF893D0.toInt()
            BLUE -> 0xFF4A7CE0.toInt()
            RED -> 0xFFE05A4A.toInt()
            GREEN -> 0xFF4ACB6A.toInt()
            YELLOW -> 0xFFE0D24A.toInt()
            PURPLE -> 0xFFB05AE0.toInt()
            WHITE -> 0xFFEDEDED.toInt()
        }

    companion object {
        fun from(color: BossEvent.BossBarColor): BossBarColorFilter =
            entries.firstOrNull { it.vanilla == color } ?: ANY
    }
}
