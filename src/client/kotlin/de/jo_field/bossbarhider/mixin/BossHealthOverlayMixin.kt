package de.jo_field.bossbarhider.mixin

import de.jo_field.bossbarhider.config.BossBarHiderConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.BossHealthOverlay
import net.minecraft.client.gui.components.LerpingBossEvent
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.Locale
import java.util.UUID

// TODO: mixins are usually written in Java
@Mixin(BossHealthOverlay::class)
class BossHealthOverlayMixin {

    private val `bossbarhider$hiddenThisFrame` = HashMap<UUID, LerpingBossEvent>()

    @Inject(
        method = ["extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"],
        at = [At("HEAD")]
    )
    private fun `bossbarhider$hideBeforeExtract`(extractor: GuiGraphicsExtractor, ci: CallbackInfo) {
        `bossbarhider$hiddenThisFrame`.clear()

        if (!BossBarHiderConfig.isHiderEnabled) {
            return
        }

        val needles = BossBarHiderConfig.stringsToHide
            .filter { it.isNotBlank() }
            .map { it.lowercase(Locale.ROOT) }

        if (needles.isEmpty()) {
            return
        }

        val events = (this as BossHealthOverlayAccessor).`bossbarhider$getEvents`()
        val iterator = events.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val title = entry.value.name?.string.orEmpty().lowercase(Locale.ROOT)

            if (needles.any { title.contains(it) }) {
                `bossbarhider$hiddenThisFrame`[entry.key] = entry.value
                iterator.remove()
            }
        }
    }

    @Inject(
        method = ["extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"],
        at = [At("RETURN")]
    )
    private fun `bossbarhider$restoreAfterExtract`(extractor: GuiGraphicsExtractor, ci: CallbackInfo) {
        if (`bossbarhider$hiddenThisFrame`.isEmpty()) {
            return
        }

        val events = (this as BossHealthOverlayAccessor).`bossbarhider$getEvents`()
        events.putAll(`bossbarhider$hiddenThisFrame`)
        `bossbarhider$hiddenThisFrame`.clear()
    }
}
