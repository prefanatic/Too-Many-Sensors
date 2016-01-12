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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogData
import io.github.prefanatic.toomanysensors.extension.bindView
import rx.subjects.PublishSubject

class LogDataAdapter(data: MutableList<LogData>) : ListAdapter<LogData, LogDataAdapter.ViewHolder>(data) {

    private val clickSubject: PublishSubject<ClickEvent> = PublishSubject.create()

    override fun onBindViewHolder(holder: LogDataAdapter.ViewHolder, obj: LogData) {
        holder.apply {
            name.text = obj.sensorName
        }
    }

    public fun getClickObservable() = clickSubject.asObservable()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): LogDataAdapter.ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_log_data, parent, false))
        viewHolder.getClickObservable().map { ClickEvent(data[viewHolder.adapterPosition], viewHolder) }.subscribe { clickSubject.onNext(it) }

        return viewHolder
    }

    inner class ClickEvent(val obj: LogData, val viewHolder: LogDataAdapter.ViewHolder)

    inner class ViewHolder(itemView: View?) : ObservableViewHolder(itemView) {
        val name by bindView<TextView>(R.id.sensor_name)
    }
}