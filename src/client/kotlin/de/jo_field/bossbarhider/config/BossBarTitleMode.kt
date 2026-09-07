package de.jo_field.bossbarhider.config

enum class BossBarTitleMode(val displayName: String) {
    STRING("Contains String"),
    EQUALS_IGNORE_CASE("Contains UpperLower"),
    REGEX("Regex");

    fun next(): BossBarTitleMode = entries[(ordinal + 1) % entries.size]
}
