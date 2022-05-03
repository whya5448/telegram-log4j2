package org.metalscraps.discord.bot.telegram

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
import org.telegram.telegrambots.meta.generics.BotSession
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat

@Plugin(name = "Telegram", category = "Core", elementType = "appender", printObject = true)
class TelegramAppender(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable?>?,
    ignoreExceptions: Boolean,
    token: String,
    val chatId: String,
) : AbstractAppender(
    name,
    filter,
    layout,
    ignoreExceptions,
    arrayOf()
) {
    private val telegram = Telegram(token)
    private var session: BotSession;

    init {
        val session = DefaultBotSession()
        session.setUpdatesSupplier { emptyList() }
        session.setCallback(telegram)
        session.setOptions(telegram.options)
        session.start()

        this.session = session
    }

    override fun append(event: LogEvent?) {
        if (event == null || event.message == null) {
            return
        }
        val level = event.level //레벨
        val timemill = event.timeMillis
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timestamp: String = df.format(timemill) //타임스탬프
        val loggerName = event.loggerName //로거 이름
        val msg = event.message.formattedMessage //내용

        if (level.isMoreSpecificThan(Level.ERROR))
            telegram.sendMessage(chatId, "[$level] [$timestamp] [$loggerName] $msg")
    }

    override fun stop(timeout: Long, timeUnit: java.util.concurrent.TimeUnit?): Boolean {
        setStopping()
        session.stop()
        setStopped()
        return true
    }

    companion object {
        @JvmStatic
        @PluginFactory
        fun createAppender(
            @PluginAttribute("name") name: String?,
            @PluginElement("Layout") layout: Layout<out Serializable?>?,
            @PluginElement("Filter") filter: Filter?,
            @PluginAttribute("token") token: String?,
            @PluginAttribute("chatId") chatId: String?,
        ): TelegramAppender? {
            var layout = layout

            if (name == null) {
                LOGGER.error("Error! Name is Null")
                return null
            }

            if (layout == null) {
                layout = PatternLayout.createDefaultLayout()
            }

            if (token == null) {
                LOGGER.error("Error! token is Null")
                return null
            }

            if (chatId == null) {
                LOGGER.error("Error! chatId is Null")
                return null
            }

            return TelegramAppender(name, filter, layout, true, token, chatId)
        }
    }
}
