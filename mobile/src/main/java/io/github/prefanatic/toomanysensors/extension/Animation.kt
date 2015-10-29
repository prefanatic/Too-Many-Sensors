package io.github.prefanatic.toomanysensors.extension

import android.view.View
import android.view.ViewPropertyAnimator

fun View.simpleShow() {
    if (visibility == View.VISIBLE) return

    animate().alpha(1f).withStartAction { visibility = View.VISIBLE }}

fun View.simpleHide() {
    if (visibility == View.GONE) return

    animate().alpha(0f).withEndAction { visibility = View.GONE }
}