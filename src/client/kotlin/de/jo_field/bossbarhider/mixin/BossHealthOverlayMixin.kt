package de.jo_field.bossbarhider.mixin

import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import de.jo_field.bossbarhider.config.BossBarHiderConfig
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(BossHealthOverlay::class)
class BossHealthOverlayMixin {
    // Only filter the render iteration. Keep event order, network updates, and world effects intact.
    @ModifyExpressionValue(
        method = ["extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"],
        at = [At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;")]
    )
    private fun `bossbarhider$visibleEvents`(events: Collection<LerpingBossEvent>): Collection<LerpingBossEvent> {
        if (!BossBarHiderConfig.current.isHiderEnabled) return events
        return events.filterNot(BossBarHiderConfig::shouldHide)
    }
}
