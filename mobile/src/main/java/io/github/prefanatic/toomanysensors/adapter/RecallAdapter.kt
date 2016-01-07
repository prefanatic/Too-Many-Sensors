package io.github.prefanatic.toomanysensors.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import java.text.SimpleDateFormat
import java.util.*

class RecallAdapter : ListAdapter<LogEntry, RecallAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, obj: LogEntry) {
        holder.apply {
            name.text = obj.name
            recordedAt.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(obj.dateCollected))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_recall_event, parent, false))

    inner class ViewHolder : RecyclerView.ViewHolder {
        val name by bindView<TextView>(R.id.event_title)
        val recordedAt by bindView<TextView>(R.id.event_recorded_at)

        constructor(itemView: View?) : super(itemView) {

        }
    }
}