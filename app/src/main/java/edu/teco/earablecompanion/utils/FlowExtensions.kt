package edu.teco.earablecompanion.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <V> MutableStateFlow<List<V>>.clear() {
    value = emptyList()
}

fun <V> MutableStateFlow<List<V>>.set(items: List<V>) {
    value = items
}
