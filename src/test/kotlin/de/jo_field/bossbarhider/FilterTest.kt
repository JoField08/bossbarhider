package de.jo_field.bossbarhider

import de.jo_field.bossbarhider.config.*
import de.jo_field.bossbarhider.filter.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Locale

class FilterTest {
    private val bar = BossBarState("The WITHER King", BarColor.PURPLE, true, false, true)

    @Test fun `title matching retains case insensitive legacy behavior in any locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertTrue(BossBarFilter.matches(FilterRule(title = "wither"), bar))
            assertFalse(BossBarFilter.matches(FilterRule(title = "dragon"), bar))
        } finally { Locale.setDefault(previous) }
    }

    @Test fun `empty and disabled rules never hide everything`() {
        assertFalse(BossBarFilter.matches(FilterRule(), bar))
        assertFalse(BossBarFilter.matches(FilterRule(title = "  "), bar))
        assertFalse(BossBarFilter.matches(FilterRule(enabled = false, color = BarColor.PURPLE), bar))
    }

    @Test fun `color only rule rejects other colors`() {
        val rule = FilterRule(color = BarColor.PURPLE)
        assertTrue(BossBarFilter.matches(rule, bar))
        assertFalse(BossBarFilter.matches(rule, bar.copy(color = BarColor.RED)))
    }

    @Test fun `each flag can require either presence or absence`() {
        assertTrue(BossBarFilter.matches(FilterRule(darkenScreen = FlagCondition.PRESENT), bar))
        assertFalse(BossBarFilter.matches(FilterRule(darkenScreen = FlagCondition.ABSENT), bar))
        assertTrue(BossBarFilter.matches(FilterRule(bossMusic = FlagCondition.ABSENT), bar))
        assertFalse(BossBarFilter.matches(FilterRule(bossMusic = FlagCondition.PRESENT), bar))
        assertTrue(BossBarFilter.matches(FilterRule(worldFog = FlagCondition.PRESENT), bar))
        assertFalse(BossBarFilter.matches(FilterRule(worldFog = FlagCondition.ABSENT), bar))
    }

    @Test fun `conditions combine with AND and rules with OR respecting global toggle`() {
        val rule = FilterRule(title = "wither", color = BarColor.PURPLE, bossMusic = FlagCondition.ABSENT)
        assertTrue(BossBarFilter.matches(rule, bar))
        assertFalse(BossBarFilter.matches(rule, bar.copy(title = "Dragon")))
        assertFalse(BossBarFilter.matches(rule, bar.copy(bossMusic = true)))
        assertFalse(BossBarFilter.matches(rule, bar.copy(color = BarColor.BLUE)))
        val config = ConfigData(rules = listOf(FilterRule(title = "dragon"), rule))
        assertTrue(BossBarFilter.shouldHide(config, bar))
        assertFalse(BossBarFilter.shouldHide(config.copy(isHiderEnabled = false), bar))
    }

    @Test fun `exact prefix and suffix modes use the intended boundary`() {
        assertTrue(BossBarFilter.matches(FilterRule(title = "the wither king", titleMode = TitleMode.EXACT), bar))
        assertFalse(BossBarFilter.matches(FilterRule(title = "wither", titleMode = TitleMode.EXACT), bar))
        assertTrue(BossBarFilter.matches(FilterRule(title = "the", titleMode = TitleMode.STARTS_WITH), bar))
        assertFalse(BossBarFilter.matches(FilterRule(title = "wither", titleMode = TitleMode.STARTS_WITH), bar))
        assertTrue(BossBarFilter.matches(FilterRule(title = "king", titleMode = TitleMode.ENDS_WITH), bar))
        assertFalse(BossBarFilter.matches(FilterRule(title = "wither", titleMode = TitleMode.ENDS_WITH), bar))
    }
}
