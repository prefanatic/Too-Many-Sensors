package io.github.prefanatic.toomanysensors.extension

import android.app.Activity
import android.content.Intent
import android.os.Build

/*
public fun Activity.showActivity(activityClass: Class) {
    val intent = Intent()
    startActivity(intent, activityClass)
}*/

fun Activity.setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        window.statusBarColor = color
}