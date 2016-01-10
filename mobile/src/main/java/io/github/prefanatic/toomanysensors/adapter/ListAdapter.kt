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