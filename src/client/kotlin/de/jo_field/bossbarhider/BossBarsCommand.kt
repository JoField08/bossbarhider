package de.jo_field.bossbarhider

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.jo_field.bossbarhider.mixin.BossHealthOverlayAccessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.network.chat.Component
import java.util.Locale
import java.util.UUID

fun literal(name: String): LiteralArgumentBuilder<FabricClientCommandSource> =
    LiteralArgumentBuilder.literal(name)

object BossBarsCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("bossbar-debug")
                            .executes { context -> listBossBars(context.source) }
            )
        }
    }

    private fun listBossBars(source: FabricClientCommandSource): Int {
        val overlay = Minecraft.getInstance().gui.hud.bossOverlay
        val events = (overlay as BossHealthOverlayAccessor).`bossbarhider$getEvents`()

        if (events.isEmpty()) {
            source.sendFeedback(
                Component.literal("No active bossbars found.")
                    .withStyle(ChatFormatting.RED)
            )
            return 0
        }

        // header
        val header = Component.empty()
            .append(Component.literal("=== ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Active Bossbars ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("(${events.size}) ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("===").withStyle(ChatFormatting.DARK_GRAY))

        source.sendFeedback(header)

        // entries
        events.forEach { (id, event) ->
            source.sendFeedback(formatBossBar(id, event))
        }

        return events.size
    }

    private fun formatBossBar(
        id: UUID,
        event: LerpingBossEvent
    ): Component {
        val titleText = event.name.string
            .replace("\n", "\\n")
            .replace("\r", "\\r")

        val percent = (event.progress * 100).toInt()
        val colorName = event.color.name.lowercase(Locale.ROOT)
        val overlayName = event.overlay.name.lowercase(Locale.ROOT)

        val message = Component.empty()
            // Title & Percent
            .append(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(titleText).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal(" ($percent%)\n").withStyle(ChatFormatting.GREEN))
            // ID
            .append(Component.literal("   ID: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(id.toString()).withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\n"))
            // Design (Farbe & Overlay)
            .append(Component.literal("   Style: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("$colorName / $overlayName").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("\n"))
            // Flags (Darken / Music / Fog)
            .append(Component.literal("   Flags: ").withStyle(ChatFormatting.GRAY))
            .append(formatFlag("Darken", event.shouldDarkenScreen()))
            .append(Component.literal(" "))
            .append(formatFlag("Music", event.shouldPlayBossMusic()))
            .append(Component.literal(" "))
            .append(formatFlag("Fog", event.shouldCreateWorldFog()))

        return message
    }

    private fun formatFlag(name: String, active: Boolean): Component {
        val color = if (active) ChatFormatting.DARK_GREEN else ChatFormatting.DARK_RED
        val prefix = if (active) "+" else "-"
        return Component.literal("[$prefix$name]").withStyle(color)
    }
}