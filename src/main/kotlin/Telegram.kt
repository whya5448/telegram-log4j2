package org.metalscraps.discord.bot.telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

class Telegram(private val token: String) : TelegramLongPollingBot() {
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

    fun sendMessage(chatId: String, text: String) {
        //val executeAsync: CompletableFuture<Message> = executeAsync(SendMessage(chatId, text))
        execute(SendMessage(chatId, text))
    }
}