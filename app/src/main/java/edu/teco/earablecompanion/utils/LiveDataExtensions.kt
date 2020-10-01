package edu.teco.earablecompanion.utils

import androidx.lifecycle.MutableLiveData

inline fun <V> MutableLiveData<V>.update(action: (V) -> V) {
    value?.let {
        val newValue = action(it)
        value = newValue
    }
}