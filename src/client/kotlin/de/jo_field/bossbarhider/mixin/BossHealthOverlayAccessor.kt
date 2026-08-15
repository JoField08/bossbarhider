package de.jo_field.bossbarhider.mixin

import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import java.util.UUID

/**
 * Accessor-Mixin: gibt uns Lese-/Schreibzugriff auf das private Feld,
 * in dem die BossHealthOverlay die aktuell aktiven BossBars haelt.
 *
 * Verifiziert gegen die im Projekt gebundelte MC-26.2-Client-Jar:
 * net.minecraft.client.gui.components.BossHealthOverlay#events
 * vom Typ Map<UUID, LerpingBossEvent>.
 */
@Mixin(BossHealthOverlay::class)
interface BossHealthOverlayAccessor {

    @Accessor("events")
    fun `bossbarhider$getEvents`(): MutableMap<UUID, LerpingBossEvent>
}
