package io.github.prefanatic.toomanysensors.extension

import android.content.Context

fun Context.getColor(resId: Int): Int
        = resources.getColor(resId, theme)