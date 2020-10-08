package edu.teco.earablecompanion.utils

import androidx.lifecycle.MutableLiveData

inline fun <T> MutableLiveData<T>.update(action: (T) -> T) {
    value?.let {
        val newValue = action(it)
        value = newValue
    }
}