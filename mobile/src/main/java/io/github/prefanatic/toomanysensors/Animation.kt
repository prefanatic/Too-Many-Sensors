package io.github.prefanatic.toomanysensors

import android.view.View

fun View.simpleShow() {
    if (visibility == View.VISIBLE) return

    animate().alpha(1f).withStartAction { visibility = View.VISIBLE }
}

fun View.simpleHide() {
    if (visibility == View.GONE) return

    animate().alpha(0f).withEndAction { visibility = View.GONE }
}