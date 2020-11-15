package edu.teco.earablecompanion.utils.extensions

import androidx.lifecycle.LiveData

inline val LiveData<Boolean>.valueOrFalse: Boolean get() = value ?: false