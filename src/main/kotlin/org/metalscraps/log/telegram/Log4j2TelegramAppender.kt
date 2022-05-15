package org.metalscraps.log.telegram

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import java.io.Serializable
import java.util.concurrent.TimeUnit

@Plugin(name = "Telegram", category = "Core", elementType = "appender", printObject = true)
open class Log4j2TelegramAppender(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable?>?,
    ignoreExceptions: Boolean,
    token: String,
    internal val chatId: Long,
    internal val level: Level = Level.ERROR,
    internal val useCodeStyle: Boolean = true,
) : AbstractAppender(
    name, filter, layout, ignoreExceptions, arrayOf()
) {
    private val telegram: Telegram = TelegramBuilder(token).build()

    override fun append(event: LogEvent?) {
        if (event == null || event.message == null) {
            return
        }

        if (event.level.isMoreSpecificThan(level)) execute(chatId, toSerializable(event).toString())
    }

    private fun execute(chatId: Long, message: String) {
        if (useCodeStyle) {
            telegram.append(chatId, "```$message```")
        } else {
            telegram.append(chatId, message)
        }
    }

    override fun start() {
        telegram.start()
        super.start()
    }

    override fun stop(timeout: Long, timeUnit: TimeUnit?): Boolean {
        telegram.stop()
        return super.stop(timeout, timeUnit)
    }

    companion object {
        @JvmStatic
        @PluginFactory
        fun createAppender(
            @PluginAttribute("name", defaultString = "Telegram") name: String,
            @PluginElement("Layout") layout: Layout<out Serializable?>?,
            @PluginElement("Filter") filter: Filter?,
            @PluginAttribute("token") token: String?,
            @PluginAttribute("chatId") chatId: String?,
            @PluginAttribute("level", defaultString = "INFO") level: Level,
            @PluginAttribute("useCodeStyle", defaultBoolean = true) useCodeStyle: Boolean,
        ): Log4j2TelegramAppender? {
            val newLayout = layout ?: PatternLayout.createDefaultLayout()

            if (token == null) {
                LOGGER.error("Error! token is Null")
                return null
            }

            if (chatId == null) {
                LOGGER.error("Error! chatId is Null")
                return null
            }

            return Log4j2TelegramAppender(name, filter, newLayout, true, token, chatId.toLong(), level, useCodeStyle)
        }
    }
}
