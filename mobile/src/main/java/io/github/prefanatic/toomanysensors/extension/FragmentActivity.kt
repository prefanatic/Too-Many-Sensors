/*
 * Copyright 2015-2016 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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