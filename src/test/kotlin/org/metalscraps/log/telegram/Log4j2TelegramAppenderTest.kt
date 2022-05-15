package org.metalscraps.log.telegram

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.message.SimpleMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalTime
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@SpringBootApplication
internal class Log4j2TelegramAppenderTest {
    var ctx: LoggerContext? = null
    var appender: Log4j2TelegramAppender? = null

    @BeforeEach
    fun setup() {
        val ctx = LogManager.getContext(false) as LoggerContext

        this.ctx = ctx
        this.appender = ctx.configuration.appenders["Telegram"] as Log4j2TelegramAppender
    }

    @Test
    fun createAppender() {
        assertNotNull(appender)

        appender!!.let {
            assertNotNull(it.chatId)
            assertNotNull(it.level)
            assertNotNull(it.layout)
        }
    }

    @Test
    fun append() {
        assertNotNull(appender)
        val thread = Thread.currentThread()
        appender!!.let {
            fun getEvent(level: Level, message: String): Log4jLogEvent {
                return Log4jLogEvent.newBuilder()
                    .setLevel(level)
                    .setMessage(SimpleMessage(message))
                    .setLoggerName(appender!!.name)
                    .setThreadName(thread.name)
                    .setThreadId(thread.id)
                    .build()
            }

            val requestId = UUID.randomUUID().toString().replace("-", "").substring(24)
            for (i in 1..30) {
                it.append(
                    getEvent(
                        Level.ERROR,
                        "[$requestId-$i] This message must be visible -- Log4j2 Telegram appender test message"
                    )
                )
                it.append(
                    getEvent(
                        Level.DEBUG,
                        "[$requestId-$i] This message must not be visible -- Log4j2 Telegram appender test message"
                    )
                )
            }
        }

        val expireTime = LocalTime.now().plusMinutes(3)
        TelegramBuilder.instances.values.first().rateLimitQueue.queue.let {
            while (!it.isEmpty()) {
                assertTrue(expireTime.isAfter(LocalTime.now()))
                println("left queue size: ${it.size}")
                Thread.sleep(5000)
            }
        }
    }
}