package de.jo_field.bossbarhider.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import de.jo_field.bossbarhider.config.BossBarHiderConfigScreenFactory

class BossbarHiderModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent -> BossBarHiderConfigScreenFactory.create(parent) }
    }
}