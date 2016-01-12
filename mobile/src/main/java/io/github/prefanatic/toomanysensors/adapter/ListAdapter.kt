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

package io.github.prefanatic.toomanysensors.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.*

abstract class ListAdapter<O, T : RecyclerView.ViewHolder>(public val data: MutableList<O> = ArrayList()) : RecyclerView.Adapter<T>() {
    public fun add(o: O) {
        data.add(o)
        notifyItemInserted(data.size)
    }

    public fun remove(o: O) {
        val i = data.indexOf(o)
        if (i < 0) return

        data.removeAt(i)
        notifyItemRemoved(i)
    }

    override fun getItemCount() = data.size

    final override fun onBindViewHolder(holder: T, position: Int) =
            onBindViewHolder(holder, data[position])

    override abstract fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): T

    abstract fun onBindViewHolder(holder: T, obj: O)

}