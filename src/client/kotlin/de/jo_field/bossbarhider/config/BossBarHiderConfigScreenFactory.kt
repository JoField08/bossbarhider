package de.jo_field.bossbarhider.config

import de.jo_field.bossbarhider.gui.BossBarHiderScreen
import net.minecraft.client.gui.screens.Screen

object BossBarHiderConfigScreenFactory {
    fun create(parent: Screen?): Screen = BossBarHiderScreen(parent)
}
