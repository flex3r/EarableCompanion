package edu.teco.earablecompanion.utils

import androidx.lifecycle.MutableLiveData

inline fun <T> MutableLiveData<T>.update(action: T.() -> Unit) {
    value?.let {
        action(it)
        value = it
    }
}