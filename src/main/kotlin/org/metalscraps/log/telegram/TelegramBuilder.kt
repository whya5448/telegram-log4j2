package org.metalscraps.log.telegram

data class TelegramBuilder(
    internal val token: String,
    internal val queueSize: Int = 1024
) {
    companion object {
        internal val instances = mutableMapOf<String, Telegram>()
    }

    fun build(): Telegram {
        synchronized(this) {
            val token = this.token
            require(token.isNotBlank())
            synchronized(instances) {
                instances[token]?.let { return it }

                val telegram = Telegram(this)
                instances[token] = telegram
                return telegram
            }
        }
    }
}