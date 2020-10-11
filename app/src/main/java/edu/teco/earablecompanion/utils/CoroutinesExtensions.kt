package edu.teco.earablecompanion.utils

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.MutableStateFlow

inline fun <T> ConflatedBroadcastChannel<T>.updateValue(block: T.() -> Unit) {
    val current = value
    current.block()
    offer(current)
}

inline fun <T> MutableStateFlow<T>.updateValue(block: T.() -> Unit) {
    val current = value
    current.block()
    value = current
}

inline fun <T> MutableStateFlow<T>.setValue(block: () -> T) {
    value = block()
}
