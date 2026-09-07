package de.jo_field.bossbarhider.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object ConfigCodec {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun encode(config: ConfigData): String = gson.toJson(config)

    fun decode(json: String): ConfigData {
        val root = JsonParser.parseString(json)
        require(root.isJsonObject) { "Configuration must be an object" }
        val obj = root.asJsonObject
        val enabled = obj.boolean("isHiderEnabled", true)
        val rules = when {
            obj.has("rules") -> {
                require(obj["rules"].isJsonArray) { "rules must be an array" }
                obj.getAsJsonArray("rules").map { element ->
                    require(element.isJsonObject) { "Each rule must be an object" }
                    val rule = element.asJsonObject
                    FilterRule(
                        enabled = rule.boolean("enabled", true),
                        title = rule.string("title", ""),
                        titleMode = rule.enum("titleMode", TitleMode.CONTAINS),
                        color = if (!rule.has("color") || rule["color"].isJsonNull) null
                            else rule.enum("color", BarColor.PURPLE),
                        darkenScreen = rule.enum("darkenScreen", FlagCondition.ANY),
                        bossMusic = rule.enum("bossMusic", FlagCondition.ANY),
                        worldFog = rule.enum("worldFog", FlagCondition.ANY)
                    )
                }
            }
            obj.has("stringsToHide") -> {
                require(obj["stringsToHide"].isJsonArray) { "stringsToHide must be an array" }
                obj.getAsJsonArray("stringsToHide").filterNot { it.isJsonNull }.map { value ->
                    require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "Title must be text" }
                    value.asString
                }.filter { it.isNotBlank() }.map { FilterRule(title = it) }
            }
            else -> ConfigData().rules
        }
        return ConfigData(enabled, rules)
    }

    private fun JsonObject.string(key: String, fallback: String): String {
        val value = get(key) ?: return fallback
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "$key must be text" }
        return value.asString
    }

    private fun JsonObject.boolean(key: String, fallback: Boolean): Boolean {
        val value = get(key) ?: return fallback
        require(value.isJsonPrimitive && value.asJsonPrimitive.isBoolean) { "$key must be boolean" }
        return value.asBoolean
    }

    private inline fun <reified T : Enum<T>> JsonObject.enum(key: String, fallback: T): T =
        enumValueOf<T>(string(key, fallback.name))
}
