package edu.teco.earablecompanion.utils.extensions

import android.content.res.Configuration
import androidx.fragment.app.Fragment

inline val Fragment.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE