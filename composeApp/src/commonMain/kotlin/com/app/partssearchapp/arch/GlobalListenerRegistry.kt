package com.app.partssearchapp.arch

object GlobalListenerRegistry {
    private var counter = 0L

    @PublishedApi
    internal val backingMap = mutableMapOf<String, Any>()

    fun <T : Any> register(listener: T): String {
        val token = "L_" + (++counter)
        backingMap[token] = listener
        return token
    }

    inline fun <reified T : Any> getTyped(token: String): T = backingMap[token] as? T
        ?: error("Listener for token '$token' not found or wrong type ${T::class}")

    fun unregister(token: String) {
        backingMap.remove(token)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(token: String): T? = backingMap[token] as? T
}
