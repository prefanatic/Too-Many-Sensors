package io.github.prefanatic.toomanysensors.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import rx.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

class RecallAdapter(data: MutableList<LogEntry>) : ListAdapter<LogEntry, RecallAdapter.ViewHolder>(data) {
    private val clickSubject: PublishSubject<ClickEvent> = PublishSubject.create()

    override fun onBindViewHolder(holder: ViewHolder, obj: LogEntry) {
        holder.apply {
            name.text = obj.name
            recordedAt.text = SimpleDateFormat("h:mm a MM-dd-yy").format(Date(obj.dateCollected))
        }
    }

    public fun getClickObservable() = clickSubject.asObservable()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_recall_event, parent, false))
        viewHolder.getClickObservable().map { ClickEvent(data[viewHolder.adapterPosition], viewHolder) }.subscribe { clickSubject.onNext(it) }

        return viewHolder
    }

    inner class ClickEvent(val obj: LogEntry, val viewHolder: ViewHolder)

    inner class ViewHolder(itemView: View?) : ObservableViewHolder(itemView) {
        val name by bindView<TextView>(R.id.event_title)
        val recordedAt by bindView<TextView>(R.id.event_recorded_at)
    }
}
