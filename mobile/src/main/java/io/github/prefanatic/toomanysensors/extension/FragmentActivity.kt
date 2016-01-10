package io.github.prefanatic.toomanysensors.extension

import android.app.Fragment
import android.support.v4.app.FragmentActivity

public fun FragmentActivity.showFragment(frameId: Int, frag: Fragment) {
    val curFrag = fragmentManager.findFragmentById(frameId)

    if (curFrag == null) {
        fragmentManager.beginTransaction()
                .add(frameId, frag)
                .commit()
    } else {
        fragmentManager.beginTransaction()
                .replace(frameId, frag)
                .addToBackStack(null)
                .commit()
    }
}

public fun <T : Fragment> FragmentActivity.showFragment(frameId: Int, fragmentClass: () -> T)
        = showFragment(frameId, fragmentClass())