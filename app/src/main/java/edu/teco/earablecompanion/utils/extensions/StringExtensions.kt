package edu.teco.earablecompanion.utils.extensions

fun String?.notBlankOrNull() = when {
    this.isNullOrBlank() -> null
    else -> this
}