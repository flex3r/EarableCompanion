package edu.teco.earablecompanion.utils

import kotlinx.coroutines.channels.ConflatedBroadcastChannel

inline fun <T> ConflatedBroadcastChannel<T>.updateValue(block: T.() -> Unit) {
    val current = value
    current.block()
    offer(current)
}
