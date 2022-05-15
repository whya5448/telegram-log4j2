package org.metalscraps.log.telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.BotSession
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.lang.Thread.interrupted
import java.lang.Thread.sleep
import java.time.LocalTime
import java.util.concurrent.LinkedBlockingQueue

class Telegram internal constructor(builder: TelegramBuilder) : TelegramLongPollingBot(), Runnable {
    private val token: String = builder.token
    private val session: BotSession
    private val thread: Thread
    private val stopped = false
    internal val rateLimitQueue = RateLimitQueue(builder.queueSize)

    init {

        val session = DefaultBotSession()
        session.setUpdatesSupplier { emptyList() }
        session.setCallback(this)
        session.setOptions(this.options)

        val thread = Thread(this)
        thread.name = "$botUsername Message Consumer"

        this.session = session
        this.thread = thread
    }

    @Throws(TelegramApiRequestException::class)
    override fun clearWebhook() {
    }

    override fun getBotToken(): String {
        return token
    }

    override fun getBotUsername(): String {
        return "Metalscraps_Bot"
    }

    override fun onUpdateReceived(update: Update?) {}

    fun start() {
        session.start()
        thread.start()
    }

    fun stop() {
        thread.interrupt()
        session.stop()
    }

    fun append(chatId: Long, text: String) {
        if (stopped) throw IllegalStateException("Telegram appender stopped")
        rateLimitQueue.add(chatId to text)
    }

    override fun run() {
        while (!interrupted()) {
            rateLimitQueue.let {
                it.peek()?.apply {
                    while (!it.allowed()) {
                        sleep(1000L)
                    }

                    try {
                        val request = SendMessage(first.toString(), second)
                        request.parseMode = "markdown"
                        execute(request)
                        it.poll()
                    } catch (e: TelegramApiRequestException) {
                        if (e.errorCode == 429) {
                            it.exploded()
                        } else {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    sleep(35)
                }
            }

        }
    }

    internal class RateLimitQueue(size: Int) {
        companion object {
            // https://core.telegram.org/bots/faq
            // 20 messages per minute to the same group
            // BUT error at send 20!!
            private const val limit = 19
        }

        private var lastResetTime: LocalTime
        private var count = limit

        init {
            lastResetTime = LocalTime.now().plusMinutes(1)
            reset()
        }

        internal val queue = LinkedBlockingQueue<Pair<Long, String>>(size)

        fun add(pair: Pair<Long, String>) {
            queue.offer(pair)
        }

        fun peek(): Pair<Long, String>? {
            return queue.peek()
        }

        fun poll(): Pair<Long, String>? {
            return queue.poll()
        }

        fun exploded() {
            count = 0
        }

        fun allowed(): Boolean {
            if (count-- > 0) {
                return true
            }

            if (LocalTime.now().isAfter(lastResetTime)) {
                reset()
                return true
            }

            return false
        }

        private fun reset() {
            count = limit
            lastResetTime = LocalTime.now().plusMinutes(1)
        }
    }
}