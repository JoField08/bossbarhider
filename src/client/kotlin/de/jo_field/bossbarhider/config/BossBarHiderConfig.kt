package de.jo_field.bossbarhider.config

import de.jo_field.bossbarhider.BossBarHiderClient
import de.jo_field.bossbarhider.filter.BossBarFilter
import de.jo_field.bossbarhider.filter.BossBarState
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.BossEvent
import java.nio.file.Files

object BossBarHiderConfig {
    private val path by lazy { FabricLoader.getInstance().configDir.resolve("bossbarhider.json") }
    private val file by lazy { ConfigFile(path) }

    var current = ConfigData()
        private set
    var loadFailed = false
        private set

    fun load() {
        try {
            current = file.read()
            loadFailed = false
            if (!Files.exists(path)) file.write(current)
        } catch (ex: Exception) {
            current = ConfigData()
            loadFailed = true
            BossBarHiderClient.LOGGER.warn("Could not load BossBarHider configuration; original file preserved.", ex)
        }
    }

    /** Publish the snapshot only after it has been persisted successfully. */
    fun save(config: ConfigData): Boolean = try {
        val snapshot = config.copy(rules = config.rules.toList())
        if (loadFailed && Files.exists(path)) {
            val backup = Files.createTempFile(path.parent, "bossbarhider-invalid-", ".json")
            Files.copy(path, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
        file.write(snapshot)
        current = snapshot
        loadFailed = false
        true
    } catch (ex: Exception) {
        BossBarHiderClient.LOGGER.warn("Could not save BossBarHider configuration.", ex)
        false
    }

    fun shouldHide(event: BossEvent): Boolean = BossBarFilter.shouldHide(current, BossBarState(
        event.name.string, BarColor.valueOf(event.color.name),
        event.shouldDarkenScreen(), event.shouldPlayBossMusic(), event.shouldCreateWorldFog()
    ))
}
