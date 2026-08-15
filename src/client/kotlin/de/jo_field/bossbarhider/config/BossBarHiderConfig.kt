package de.jo_field.bossbarhider.config

import com.google.gson.GsonBuilder
import de.jo_field.bossbarhider.BossBarHiderClient
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files

object BossBarHiderConfig {

    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_PATH = FabricLoader.getInstance().configDir.resolve("bossbarhider.json")

    var isHiderEnabled: Boolean = true
    var stringsToHide: List<String> = listOf("Example BossBarTitle")

    private data class ConfigData(
        val stringsToHide: List<String> = listOf("Example BossBarTitle"),
        val isHiderEnabled: Boolean = true
    )

    fun load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Files.newBufferedReader(CONFIG_PATH).use { reader ->
                    val data = GSON.fromJson(reader, ConfigData::class.java)
                    if (data != null) {
                        isHiderEnabled = data.isHiderEnabled
                        stringsToHide = data.stringsToHide
                    }
                }
            } else {
                save()
            }
        } catch (e: IOException) {
            BossBarHiderClient.LOGGER.warn("Could not load BossBarHider configuration; using default values.", e)
        }
    }

    fun save() {
        try {
            Files.createDirectories(CONFIG_PATH.parent)
            Files.newBufferedWriter(CONFIG_PATH).use { writer ->
                GSON.toJson(
                    ConfigData(
                        isHiderEnabled = isHiderEnabled,
                        stringsToHide = stringsToHide
                    ),
                    writer
                )
            }
        } catch (e: IOException) {
            BossBarHiderClient.LOGGER.warn("Could not save BossBarHider configuration.", e)
        }
    }
}