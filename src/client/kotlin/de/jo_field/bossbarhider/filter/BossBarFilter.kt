package de.jo_field.bossbarhider.filter

import de.jo_field.bossbarhider.config.*

data class BossBarState(
    val title: String,
    val color: BarColor = BarColor.PURPLE,
    val darkenScreen: Boolean = false,
    val bossMusic: Boolean = false,
    val worldFog: Boolean = false
)

/** Pure matching is shared by the live HUD and the configuration tester. */
object BossBarFilter {
    fun shouldHide(config: ConfigData, bar: BossBarState): Boolean =
        config.isHiderEnabled && config.rules.any { matches(it, bar) }

    fun matches(rule: FilterRule, bar: BossBarState): Boolean {
        if (!rule.enabled || !rule.hasConditions) return false
        if (rule.color != null && rule.color != bar.color) return false
        if (!rule.darkenScreen.matches(bar.darkenScreen) ||
            !rule.bossMusic.matches(bar.bossMusic) || !rule.worldFog.matches(bar.worldFog)) return false
        if (rule.title.isBlank()) return true
        return when (rule.titleMode) {
            TitleMode.CONTAINS -> bar.title.contains(rule.title, ignoreCase = true)
            TitleMode.EXACT -> bar.title.equals(rule.title, ignoreCase = true)
            TitleMode.STARTS_WITH -> bar.title.startsWith(rule.title, ignoreCase = true)
            TitleMode.ENDS_WITH -> bar.title.endsWith(rule.title, ignoreCase = true)
        }
    }
}
