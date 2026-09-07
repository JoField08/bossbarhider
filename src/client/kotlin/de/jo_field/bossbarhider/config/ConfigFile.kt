package de.jo_field.bossbarhider.config

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

class ConfigFile(private val path: Path) {
    fun read(): ConfigData = if (Files.exists(path)) ConfigCodec.decode(Files.readString(path)) else ConfigData()

    fun write(config: ConfigData) {
        val target = path.toAbsolutePath()
        Files.createDirectories(target.parent)
        val temp = Files.createTempFile(target.parent, "bossbarhider-", ".tmp")
        try {
            Files.writeString(temp, ConfigCodec.encode(config))
            try {
                Files.move(temp, target, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp, target, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temp)
        }
    }
}
