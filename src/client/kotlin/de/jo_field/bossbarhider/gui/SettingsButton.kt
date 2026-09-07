package de.jo_field.bossbarhider.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component

/** Native button input/narration with the flat panels and blue accents used by CleanChat. */
class SettingsButton(
    private val font: Font, x: Int, y: Int, w: Int, label: Component,
    private val primary: Boolean = false, action: (Button) -> Unit
) : Button(x, y, w.coerceAtLeast(1), 20, label, OnPress { action(it) }, CreateNarration { it.get() }) {
    var swatch: Int? = null
    var compactLabel: String? = null

    init { setTooltip(Tooltip.create(label)) }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val highlighted = active && (isHovered || isFocused)
        val background = when {
            primary && highlighted -> 0xFF397ACA.toInt()
            primary -> 0xFF285797.toInt()
            highlighted -> 0xFF30343A.toInt()
            else -> 0xEE14171C.toInt()
        }
        graphics.fill(x, y, right, bottom, background)
        graphics.outline(x, y, width, height,
            if (highlighted || primary) 0xFF65B2FF.toInt() else 0xFF4B525C.toInt())
        val inset = if (swatch == null) 6 else 18
        swatch?.let { graphics.fill(x + 5, y + 6, x + 12, y + 14, it or 0xFF000000.toInt()) }
        val text = font.plainSubstrByWidth(compactLabel ?: message.string, (width - inset - 6).coerceAtLeast(0))
        graphics.text(font, text, x + inset, y + 6,
            if (active) 0xFFF1F4F8.toInt() else 0xFF858A91.toInt(), false)
    }
}
