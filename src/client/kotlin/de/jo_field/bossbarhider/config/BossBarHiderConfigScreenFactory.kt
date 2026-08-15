package de.jo_field.bossbarhider.config

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object BossBarHiderConfigScreenFactory {

    fun create(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("title.bossbarhider.config"))
            .setSavingRunnable(BossBarHiderConfig::save)

        val entryBuilder: ConfigEntryBuilder = builder.entryBuilder()

        val generalCategory: ConfigCategory = builder.getOrCreateCategory(
            Component.translatable("category.bossbarhider.general")
        )

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle(
                Component.translatable("option.bossbarhider.enabled"),
                BossBarHiderConfig.isHiderEnabled
            )
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.bossbarhider.enabled.tooltip"))
                .setSaveConsumer { value -> BossBarHiderConfig.isHiderEnabled = value }
                .build()
        )

        generalCategory.addEntry(
            entryBuilder.startStrList(
                Component.translatable("option.bossbarhider.strings_to_hide"),
                BossBarHiderConfig.stringsToHide
            )
                .setDefaultValue(listOf("Example BossBarTitle"))
                .setTooltip(Component.translatable("option.bossbarhider.strings_to_hide.tooltip"))
                .setSaveConsumer { value -> BossBarHiderConfig.stringsToHide = value }
                .build()
        )

        return builder.build()
    }
}
