package de.jo_field.bossbarhider.config

enum class TitleMode { CONTAINS, EXACT, STARTS_WITH, ENDS_WITH }

enum class BarColor(val rgb: Int) {
    PINK(0xEC81B5), BLUE(0x4C7FDB), RED(0xDC4545), GREEN(0x58B54C),
    YELLOW(0xE9D34F), PURPLE(0xA166D9), WHITE(0xE5E5E5)
}

enum class FlagCondition {
    ANY, PRESENT, ABSENT;

    fun matches(value: Boolean) = when (this) {
        ANY -> true
        PRESENT -> value
        ABSENT -> !value
    }
}

data class FilterRule(
    val enabled: Boolean = true,
    val title: String = "",
    val titleMode: TitleMode = TitleMode.CONTAINS,
    val color: BarColor? = null,
    val darkenScreen: FlagCondition = FlagCondition.ANY,
    val bossMusic: FlagCondition = FlagCondition.ANY,
    val worldFog: FlagCondition = FlagCondition.ANY
) {
    val hasConditions: Boolean get() = title.isNotBlank() || color != null ||
        darkenScreen != FlagCondition.ANY || bossMusic != FlagCondition.ANY || worldFog != FlagCondition.ANY
}

data class ConfigData(
    val isHiderEnabled: Boolean = true,
    val rules: List<FilterRule> = listOf(FilterRule(title = "Example BossBarTitle"))
)
