package edu.teco.earablecompanion.utils.extensions

import androidx.lifecycle.MutableLiveData

inline fun <T> MutableLiveData<T>.update(action: T.() -> Unit) {
    value?.let {
        action(it)
        value = it
    }
}