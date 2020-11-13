package edu.teco.earablecompanion.settings.label

sealed class LabelItem {
    object Add : LabelItem()
    data class Label(var name: String) : LabelItem()
}