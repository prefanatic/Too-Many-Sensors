package io.github.prefanatic.toomanysensors.extension

import android.app.Fragment

// This is so cool!!!
public fun Fragment.runOnUiThread(a: () -> Unit) {
    activity.runOnUiThread { a() }
}

