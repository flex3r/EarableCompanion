package edu.teco.earablecompanion.utils.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

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

inline fun <T> Flow<T>.observe(viewLifecycleOwner: LifecycleOwner, crossinline action: (value: T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        collect {
            action(it)
        }
    }
}
