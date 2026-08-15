package de.jo_field.bossbarhider.mixin

import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import java.util.UUID

@Mixin(BossHealthOverlay::class)
interface BossHealthOverlayAccessor {

    @Accessor("events")
    fun `bossbarhider$getEvents`(): MutableMap<UUID, LerpingBossEvent>
}
