package edu.teco.earablecompanion.utils

import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.showOrHide(show: Boolean) = when {
    show && !isVisible -> show()
    !show && isVisible -> hide()
    else -> Unit
}

fun ExtendedFloatingActionButton.showOrHide(show: Boolean) = when {
    show && !isVisible -> show()
    !show && isVisible -> hide()
    else -> Unit
}