package de.jo_field.bossbarhider.gui

import de.jo_field.bossbarhider.config.*
import de.jo_field.bossbarhider.filter.BossBarFilter
import de.jo_field.bossbarhider.filter.BossBarState
import de.jo_field.bossbarhider.mixin.BossHealthOverlayAccessor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import java.util.Locale

/** Draft state survives widget rebuilds and is committed only by Save. */
class BossBarHiderScreen(private val parent: Screen?) : Screen(tr("title")) {
    private class DraftRule(var value: FilterRule)
    private data class Row(val draft: DraftRule, val widgets: List<Pair<AbstractWidget, Int>>)

    private var enabled = BossBarHiderConfig.current.isHiderEnabled
    private val rules = BossBarHiderConfig.current.rules.map { DraftRule(it) }.toMutableList()
    private val viewport = RuleViewport()
    private val fixed = mutableListOf<AbstractWidget>()
    private val rows = mutableListOf<Row>()
    private var sample = BossBarState("Wither", BarColor.PURPLE, true, true, false)
    private var expanded = true
    private var saveFailed = false
    private var dragging = false
    private var thumbGrab = 0.0
    private var activeBarIndex = 0
    private var left = 0
    private var right = 0
    private var listRight = 0
    private var bannerY = 0
    private var testerY = 0
    private var rulesY = 0
    private var listTop = 0
    private var listBottom = 0
    private var footerY = 0
    private var narrow = false
    private var rowHeight = 0
    private var pendingFocus: AbstractWidget? = null
    private val white = 0xFFF1F4F8.toInt()
    private val muted = 0xFFA4AEBB.toInt()
    private val accent = 0xFF65B2FF.toInt()
    private val green = 0xFF84D69B.toInt()
    private val red = 0xFFFF8A86.toInt()

    override fun init() {
        fixed.clear()
        rows.clear()
        pendingFocus = null
        dragging = false
        left = maxOf((width - 960) / 2, (width / 24).coerceIn(10, 28))
        right = width - left
        listRight = right - 12
        narrow = right - left < 580
        rowHeight = if (narrow) 124 else 100
        bannerY = if (height < 340) 36 else 46
        testerY = bannerY + 24
        footerY = height - if (narrow) 52 else 36
        listBottom = footerY - 6
        // The tester yields space to the rule editor on small GUI scales/windows.
        if (expanded && listBottom - (testerY + 114 + 44) < minOf(rowHeight, 60)) expanded = false
        rulesY = if (expanded) testerY + 114 else testerY + 4
        listTop = rulesY + 44

        fixed += button(right - 106, 9, 106, powerLabel()) {
            enabled = !enabled
            label(it, powerLabel())
        }
        fixed += button(left, bannerY, right - left, testerLabel()) {
            expanded = !expanded
            rebuildWidgets()
        }.also { it.setTooltip(Tooltip.create(tr("tester.tooltip"))) }
        if (expanded) buildTester()
        fixed += button(right - 106, rulesY + 3, 106, tr("add"), primary = true) {
            rules += DraftRule(FilterRule())
            rebuildWidgets()
            viewport.scrollTo(viewport.maxOffset)
            positionRows()
            // Focus the new title and ensure it is visible even in a short viewport.
            rows.lastOrNull()?.widgets?.firstOrNull { it.first is EditBox }?.first?.let {
                pendingFocus = it
            }
        }
        val footerButtonsY = footerY + if (narrow) 25 else 9
        fixed += button(right - 204, footerButtonsY, 98, tr("cancel")) { onClose() }
        fixed += button(right - 98, footerButtonsY, 98, tr("save"), primary = true) {
            if (BossBarHiderConfig.save(snapshot())) onClose() else saveFailed = true
        }.also { it.setTooltip(Tooltip.create(tr("save.tooltip"))) }
        rules.forEach { rows += buildRuleRow(it) }
        viewport.resize(rows.size * rowHeight, listBottom - listTop)
        positionRows()
    }

    private fun snapshot() = ConfigData(enabled, rules.map { it.value })
    private fun powerLabel() = tr(if (enabled) "enabled" else "disabled")
    private fun testerLabel() = tr(if (expanded) "tester.collapse" else "tester.expand")
    private fun colorLabel(color: BarColor?) = tr("color.value", colorName(color))
    private fun colorName(color: BarColor?) = tr("color.${color?.name?.lowercase(Locale.ROOT) ?: "any"}")
    private fun modeLabel(mode: TitleMode) = tr("mode.${mode.name.lowercase(Locale.ROOT)}")
    private fun flagLabel(key: String, value: FlagCondition) =
        tr("flag.value", tr(key), tr("condition.${value.name.lowercase(Locale.ROOT)}"))
    private fun sampleFlagLabel(key: String, value: Boolean) = tr("flag.value", tr(key), tr(if (value) "yes" else "no"))

    private fun buildTester() {
        val x = left + 8
        val w = right - left - 16
        val colorWidth = minOf(130, w / 3)
        fixed += editBox(x, testerY + 6, w - colorWidth - 6, tr("tester.title"), sample.title) {
            sample = sample.copy(title = it)
        }
        fixed += button(x + w - colorWidth, testerY + 6, colorWidth, colorLabel(sample.color)) {
            sample = sample.copy(color = next(BarColor.entries, sample.color))
            label(it, colorLabel(sample.color))
            (it as SettingsButton).swatch = sample.color.rgb
        }.also { it.swatch = sample.color.rgb }
        val column = (w - 12) / 3
        fixed += button(x, testerY + 32, column, sampleFlagLabel("flag.darken", sample.darkenScreen)) {
            sample = sample.copy(darkenScreen = !sample.darkenScreen)
            label(it, sampleFlagLabel("flag.darken", sample.darkenScreen))
        }
        fixed += button(x + column + 6, testerY + 32, column, sampleFlagLabel("flag.music", sample.bossMusic)) {
            sample = sample.copy(bossMusic = !sample.bossMusic)
            label(it, sampleFlagLabel("flag.music", sample.bossMusic))
        }
        fixed += button(x + 2 * (column + 6), testerY + 32, w - 2 * (column + 6), sampleFlagLabel("flag.fog", sample.worldFog)) {
            sample = sample.copy(worldFog = !sample.worldFog)
            label(it, sampleFlagLabel("flag.fog", sample.worldFog))
        }
        fixed += button(x + w - 132, testerY + 59, 132, tr("tester.active")) {
            val events = (minecraft.gui.hud.bossOverlay as BossHealthOverlayAccessor).`bossbarhider$getEvents`().values.toList()
            if (events.isNotEmpty()) {
                val event = events[activeBarIndex.mod(events.size)]
                activeBarIndex++
                sample = BossBarState(event.name.string, BarColor.valueOf(event.color.name),
                    event.shouldDarkenScreen(), event.shouldPlayBossMusic(), event.shouldCreateWorldFog())
                rebuildWidgets()
            }
        }.also {
            it.active = minecraft.player != null &&
                (minecraft.gui.hud.bossOverlay as BossHealthOverlayAccessor).`bossbarhider$getEvents`().isNotEmpty()
            it.setTooltip(Tooltip.create(tr("tester.active.tooltip")))
        }
    }

    private fun buildRuleRow(draft: DraftRule): Row {
        val widgets = mutableListOf<Pair<AbstractWidget, Int>>()
        val x = left + 8
        val w = listRight - x - 6
        fun add(widget: AbstractWidget, dy: Int) { widgets += widget to dy }
        add(button(x + w - 112, 4, 84, tr(if (draft.value.enabled) "rule.on" else "rule.off")) {
            draft.value = draft.value.copy(enabled = !draft.value.enabled)
            label(it, tr(if (draft.value.enabled) "rule.on" else "rule.off"))
        }, 4)
        add(button(x + w - 22, 4, 22, Component.literal("×")) {
            rules.remove(draft)
            rebuildWidgets()
        }.also { it.message = tr("delete"); it.compactLabel = "×"; it.setTooltip(Tooltip.create(tr("delete"))) }, 4)
        val modeWidth = if (narrow) (w - 6) / 2 else 106
        val colorWidth = if (narrow) w - modeWidth - 6 else 126
        val titleWidth = if (narrow) w else w - modeWidth - colorWidth - 12
        add(editBox(x, 28, titleWidth, tr("rule.title"), draft.value.title) {
            draft.value = draft.value.copy(title = it)
        }.also { it.setHint(tr("rule.title.hint")) }, 28)
        val selectY = if (narrow) 52 else 28
        val modeX = if (narrow) x else x + titleWidth + 6
        add(button(modeX, selectY, modeWidth, modeLabel(draft.value.titleMode)) {
            draft.value = draft.value.copy(titleMode = next(TitleMode.entries, draft.value.titleMode))
            label(it, modeLabel(draft.value.titleMode))
        }.also { it.setTooltip(Tooltip.create(tr("mode.tooltip"))) }, selectY)
        add(button(modeX + modeWidth + 6, selectY, colorWidth, colorLabel(draft.value.color)) {
            val colors = listOf(null) + BarColor.entries
            draft.value = draft.value.copy(color = next(colors, draft.value.color))
            label(it, colorLabel(draft.value.color))
            (it as SettingsButton).swatch = draft.value.color?.rgb
        }.also { it.swatch = draft.value.color?.rgb }, selectY)
        val flagY = selectY + 24
        val column = (w - 12) / 3
        fun flag(index: Int, key: String, get: () -> FlagCondition, update: (FlagCondition) -> Unit) {
            add(button(x + index * (column + 6), flagY,
                if (index == 2) w - 2 * (column + 6) else column, flagLabel(key, get())) {
                update(next(FlagCondition.entries, get()))
                label(it, flagLabel(key, get()))
            }.also { it.setTooltip(Tooltip.create(tr("flag.tooltip", tr(key)))) }, flagY)
        }
        flag(0, "flag.darken", { draft.value.darkenScreen }) { draft.value = draft.value.copy(darkenScreen = it) }
        flag(1, "flag.music", { draft.value.bossMusic }) { draft.value = draft.value.copy(bossMusic = it) }
        flag(2, "flag.fog", { draft.value.worldFog }) { draft.value = draft.value.copy(worldFog = it) }
        return Row(draft, widgets)
    }

    private fun button(x: Int, y: Int, w: Int, text: Component, primary: Boolean = false, action: (Button) -> Unit) =
        addWidget(SettingsButton(font, x, y, w, text, primary, action))

    private fun label(button: Button, text: Component) {
        button.message = text
        button.setTooltip(Tooltip.create(text))
    }

    private fun editBox(x: Int, y: Int, w: Int, name: Component, value: String, changed: (String) -> Unit): EditBox =
        addWidget(EditBox(font, x, y, w.coerceAtLeast(1), 20, name)).also {
            it.setMaxLength(maxOf(4096, value.length))
            it.value = value
            it.setResponder(changed)
            it.setTooltip(Tooltip.create(name))
        }

    private fun positionRows() {
        rows.forEachIndexed { index, row ->
            val top = listTop + index * rowHeight - viewport.offset.toInt()
            row.widgets.forEach { (widget, dy) ->
                widget.y = top + dy
                // Keep widgets available for Tab navigation; rendering and pointer input are clipped below.
            }
        }
    }

    private fun inList(x: Double, y: Double) = x >= left && x < right && y >= listTop && y < listBottom

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontal: Double, vertical: Double): Boolean {
        if (!inList(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical)
        viewport.scrollTo(viewport.offset - vertical * 28)
        clearFocus()
        positionRows()
        return true
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (event.button() == 0 && viewport.maxOffset > 0 && inList(event.x(), event.y()) && event.x() >= listRight) {
            clearFocus()
            val thumbY = listTop + viewport.thumbTop
            thumbGrab = if (event.y() >= thumbY && event.y() < thumbY + viewport.thumbHeight)
                event.y() - thumbY else viewport.thumbHeight / 2.0
            dragging = true
            viewport.dragThumb(event.y() - listTop - thumbGrab)
            positionRows()
            return true
        }
        // Offscreen rows must never steal a click from the tester or footer.
        val rowWidgets = rows.flatMap { it.widgets }.map { it.first }
        rowWidgets.forEach { it.active = inList(event.x(), event.y()) && event.x() < listRight &&
            it.y >= listTop && it.bottom <= listBottom }
        return try { super.mouseClicked(event, doubleClick) } finally {
            rowWidgets.forEach { it.active = true }
            finishInput()
        }
    }

    override fun mouseDragged(event: MouseButtonEvent, deltaX: Double, deltaY: Double): Boolean {
        if (dragging && event.button() == 0) {
            viewport.dragThumb(event.y() - listTop - thumbGrab)
            positionRows()
            return true
        }
        return super.mouseDragged(event, deltaX, deltaY)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (event.button() == 0 && dragging) { dragging = false; return true }
        return super.mouseReleased(event)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == GLFW.GLFW_KEY_PAGE_DOWN || event.key() == GLFW.GLFW_KEY_PAGE_UP) {
            val direction = if (event.key() == GLFW.GLFW_KEY_PAGE_DOWN) 1 else -1
            viewport.scrollTo(viewport.offset + direction * viewport.height)
            clearFocus()
            positionRows()
            return true
        }
        val handled = super.keyPressed(event)
        finishInput()
        return handled
    }

    private fun finishInput() {
        // Vanilla focuses the clicked widget after its callback; it may now belong to the old widget tree.
        val previous = focused
        if (previous != null && children().none { it === previous }) clearFocus()
        pendingFocus?.let { setFocused(it) }
        pendingFocus = null
        revealFocused()
    }

    private fun revealFocused() {
        val widget = rows.asSequence().flatMap { it.widgets.asSequence() }.map { it.first }
            .firstOrNull { it.isFocused } ?: return
        when {
            widget.y < listTop -> viewport.scrollTo(viewport.offset + widget.y - listTop)
            widget.bottom > listBottom -> viewport.scrollTo(viewport.offset + widget.bottom - listBottom)
        }
        positionRows()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        graphics.fill(0, 0, width, height, 0xEE101318.toInt())
        text(graphics, title, left, 10, right - left - 116, white)
        if (height >= 340) text(graphics, tr("subtitle"), left, 27, right - left, muted)
        if (expanded) drawTester(graphics)
        panel(graphics, rulesY, listBottom - rulesY)
        text(graphics, tr("rules", rules.size), left + 8, rulesY + 9, right - left - 126, white)
        text(graphics, tr("rules.hint"), left + 8, rulesY + 30, right - left - 16, muted)
        fixed.forEach { it.extractRenderState(graphics, mouseX, mouseY, partialTick) }
        graphics.enableScissor(left, listTop, listRight, listBottom)
        rows.forEachIndexed { index, row ->
            val y = listTop + index * rowHeight - viewport.offset.toInt()
            if (y < listBottom && y + rowHeight > listTop) {
                val rule = row.draft.value
                graphics.fill(left + 3, y, listRight - 2, y + rowHeight - 5, 0xEF1C222B.toInt())
                graphics.fill(left + 3, y, left + 5, y + rowHeight - 5, if (rule.enabled) accent else muted)
                text(graphics, tr("rule.number", index + 1), left + 10, y + 10, listRight - left - 138, white)
                val status = when {
                    !rule.hasConditions -> tr("rule.empty")
                    !rule.enabled -> tr("rule.disabled")
                    expanded && BossBarFilter.matches(rule, sample) -> tr("rule.matches")
                    expanded -> tr("rule.no_match")
                    else -> tr("rule.conditions")
                }
                text(graphics, status, left + 10, y + rowHeight - 19, listRight - left - 22,
                    if (!rule.hasConditions) red else if (expanded && BossBarFilter.matches(rule, sample)) green else muted)
                row.widgets.forEach { (widget, _) ->
                    if (widget.bottom > listTop && widget.y < listBottom) {
                        val pointerInside = inList(mouseX.toDouble(), mouseY.toDouble())
                        widget.extractRenderState(graphics, if (pointerInside) mouseX else -1,
                            if (pointerInside) mouseY else -1, partialTick)
                    }
                }
            }
        }
        if (rows.isEmpty()) text(graphics, tr("rules.empty"), left + 10, listTop + 14, listRight - left - 20, muted)
        graphics.disableScissor()
        if (viewport.maxOffset > 0) {
            graphics.fill(listRight + 3, listTop, listRight + 8, listBottom, 0xFF303741.toInt())
            val y = listTop + viewport.thumbTop
            graphics.fill(listRight + 2, y, listRight + 9, y + viewport.thumbHeight, accent)
        }
        graphics.fill(left, footerY, right, footerY + 1, 0xFF4B525C.toInt())
        val footer = when {
            saveFailed -> tr("save.error")
            BossBarHiderConfig.loadFailed -> tr("load.error")
            else -> tr("draft")
        }
        text(graphics, footer, left, footerY + if (narrow) 8 else 15,
            right - left - if (narrow) 0 else 216, if (saveFailed) red else muted)
        // Base Screen handles delayed tooltips/narration; widgets are registered with addWidget, not addRenderableWidget.
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    private fun drawTester(graphics: GuiGraphicsExtractor) {
        panel(graphics, testerY, 108)
        val x = left + 8
        val barWidth = (right - left - 164).coerceAtLeast(20)
        graphics.fill(x, testerY + 64, x + barWidth, testerY + 73, 0xFF101318.toInt())
        graphics.fill(x, testerY + 64, x + barWidth * 3 / 4, testerY + 73, sample.color.rgb or 0xFF000000.toInt())
        val count = rules.count { BossBarFilter.matches(it.value, sample) }
        val result = when {
            !enabled -> tr("tester.disabled")
            count > 0 -> tr("tester.hidden", count)
            else -> tr("tester.visible")
        }
        text(graphics, result, x, testerY + 88, right - left - 16, if (enabled && count > 0) red else green)
    }

    private fun panel(graphics: GuiGraphicsExtractor, y: Int, h: Int) {
        graphics.fill(left, y, right, y + h, 0xD91C2027.toInt())
        graphics.outline(left, y, right - left, h, 0xFF414A56.toInt())
    }

    private fun text(graphics: GuiGraphicsExtractor, value: Component, x: Int, y: Int, w: Int, color: Int) {
        graphics.text(font, font.plainSubstrByWidth(value.string, w.coerceAtLeast(0)), x, y, color, false)
    }

    override fun onClose() { minecraft.gui.setScreen(parent) }
    override fun isPauseScreen() = false

    companion object {
        private fun tr(key: String, vararg args: Any): Component = Component.translatable("bossbarhider.$key", *args)
        private fun <T> next(values: List<T>, current: T): T = values[(values.indexOf(current) + 1) % values.size]
    }
}
