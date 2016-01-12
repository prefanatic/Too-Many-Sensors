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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rx.subjects.PublishSubject

abstract class ObservableAdapter<O, V : ObservableViewHolder> : ListAdapter<O, V>() {
    private val clickSubject: PublishSubject<ClickEvent> = PublishSubject.create()
    private var recycler: RecyclerView? = null
    private var viewHolderFactory: ((A: View?) -> V)? = null

    inner class ClickEvent(val obj: O, val viewHolder: V)

    public fun getClickObservable() = clickSubject.asObservable()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        recycler = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        recycler = null
    }

    public fun applyViewHolder(factory: (A: View?) -> V) {
        viewHolderFactory = factory
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): V {
        if (viewHolderFactory == null) {
            throw RuntimeException("No ViewHolderFactory applied to ObservableAdapter!")
        }
        val viewHolder = (viewHolderFactory!!)(LayoutInflater.from(parent?.context).inflate(getLayoutResource(), parent, false))

        // Is this the best way to do this?
        viewHolder.getClickObservable().map { ClickEvent(data[viewHolder.adapterPosition], viewHolder) }.subscribe { clickSubject.onNext(it) }

        return viewHolder
    }

    abstract fun getLayoutResource(): Int
}