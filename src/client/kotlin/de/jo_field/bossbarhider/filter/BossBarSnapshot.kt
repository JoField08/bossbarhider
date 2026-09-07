package de.jo_field.bossbarhider.filter

import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.world.BossEvent

/** Immutable snapshot of the boss bar properties rules can match against. */
data class BossBarSnapshot(
    val title: String,
    val color: BossEvent.BossBarColor,
    val darkenScreen: Boolean,
    val playsMusic: Boolean,
    val createsFog: Boolean,
) {
    companion object {
        fun of(event: LerpingBossEvent): BossBarSnapshot = BossBarSnapshot(
            title = event.name.string,
            color = event.color,
            darkenScreen = event.shouldDarkenScreen(),
            playsMusic = event.shouldPlayBossMusic(),
            createsFog = event.shouldCreateWorldFog(),
        )
    }
}
