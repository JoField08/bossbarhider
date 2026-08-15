package de.jo_field.bossbarhider

import de.jo_field.bossbarhider.config.BossBarHiderConfig
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

class BossBarHiderClient : ClientModInitializer {

    companion object {
        const val MOD_ID = "bossbarhider"
        val LOGGER = LoggerFactory.getLogger(MOD_ID)!!
    }

    override fun onInitializeClient() {
        BossBarsCommand.register()
        BossBarHiderConfig.load()
        LOGGER.info("BossBar Hider initialized ({} filtered title(s), enabled={})",
            BossBarHiderConfig.stringsToHide.size, BossBarHiderConfig.isHiderEnabled)
    }
}
