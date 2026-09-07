package de.jo_field.bossbarhider

import de.jo_field.bossbarhider.config.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ConfigTest {
    @TempDir lateinit var directory: Path

    @Test fun `legacy titles migrate without changing disabled state or whitespace`() {
        val config = ConfigCodec.decode("""{"isHiderEnabled":false,"stringsToHide":["Wither"," Event ","",null]}""")
        assertFalse(config.isHiderEnabled)
        assertEquals(listOf("Wither", " Event "), config.rules.map { it.title })
        assertTrue(config.rules.all { it.titleMode == TitleMode.CONTAINS && it.color == null })
    }

    @Test fun `explicit empty rules win over legacy titles`() {
        assertTrue(ConfigCodec.decode("""{"rules":[],"stringsToHide":["Wither"]}""").rules.isEmpty())
    }

    @Test fun `unknown rule values cannot accidentally broaden filtering`() {
        assertThrows(IllegalArgumentException::class.java) {
            ConfigCodec.decode("""{"rules":[{"title":"Wither","color":"invalid"}]}""")
        }
    }

    @Test fun `null missing and malformed documents are handled deliberately`() {
        assertTrue(ConfigCodec.decode("{}").isHiderEnabled)
        assertThrows(IllegalArgumentException::class.java) { ConfigCodec.decode("null") }
        assertThrows(Exception::class.java) { ConfigCodec.decode("{broken") }
        assertThrows(IllegalArgumentException::class.java) { ConfigCodec.decode("""{"rules":null}""") }
    }

    @Test fun `all rule fields survive saving and reloading`() {
        val config = ConfigData(false, listOf(FilterRule(false, "Boss", TitleMode.EXACT, BarColor.YELLOW,
            FlagCondition.PRESENT, FlagCondition.ABSENT, FlagCondition.PRESENT)))
        val path = directory.resolve("config/bossbarhider.json")
        ConfigFile(path).write(config)
        assertEquals(config, ConfigFile(path).read())
        assertFalse(Files.readString(path).contains("stringsToHide"))
    }

    @Test fun `reading corrupt config preserves original bytes`() {
        val path = directory.resolve("bossbarhider.json")
        Files.writeString(path, "{bad config")
        assertThrows(Exception::class.java) { ConfigFile(path).read() }
        assertEquals("{bad config", Files.readString(path))
    }

    @Test fun `missing config yields defaults and failed save leaves destination intact`() {
        val path = directory.resolve("bossbarhider.json")
        assertEquals(ConfigData(), ConfigFile(path).read())
        Files.createDirectory(path)
        Files.writeString(path.resolve("keep.txt"), "keep")
        assertThrows(Exception::class.java) { ConfigFile(path).write(ConfigData()) }
        assertEquals("keep", Files.readString(path.resolve("keep.txt")))
    }
}
