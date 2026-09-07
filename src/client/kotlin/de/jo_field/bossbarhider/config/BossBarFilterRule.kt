package de.jo_field.bossbarhider.config

import java.util.UUID

data class BossBarFilterRule(
    val id: String = UUID.randomUUID().toString(),
    var enabled: Boolean = true,
    var pattern: String = "",
    var mode: BossBarTitleMode = BossBarTitleMode.STRING,
    var color: BossBarColorFilter = BossBarColorFilter.ANY,
    var darken: TriState = TriState.ANY,
    var music: TriState = TriState.ANY,
    var fog: TriState = TriState.ANY,
) {
    /** A rule with no active condition would otherwise match every boss bar - guard against that. */
    fun hasCriteria(): Boolean =
        pattern.isNotBlank() || color != BossBarColorFilter.ANY ||
            darken != TriState.ANY || music != TriState.ANY || fog != TriState.ANY
}
