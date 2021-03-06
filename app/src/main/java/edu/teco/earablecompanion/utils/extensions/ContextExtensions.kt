package edu.teco.earablecompanion.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(@AttrRes themeAttrId: Int) = obtainStyledAttributes(intArrayOf(themeAttrId)).use { it.getColor(0, Color.MAGENTA) }