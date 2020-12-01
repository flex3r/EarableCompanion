package edu.teco.earablecompanion.utils.extensions

import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

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

fun View.showShortSnackbar(text: String) = Snackbar.make(this, text, Snackbar.LENGTH_SHORT).show()