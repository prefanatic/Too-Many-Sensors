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
import android.widget.CheckBox
import com.jakewharton.rxbinding.view.clicks
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.dto.WearableSensor
import io.github.prefanatic.toomanysensors.extension.bindView
import timber.log.Timber
import java.util.*

class SensorAdapter : RecyclerView.Adapter<SensorAdapter.ViewHolder>() {
    public val sensorList = ArrayList<WearableSensor>()
    private val enabledMap = HashMap<Int, Boolean>()

    public fun getSelected(): List<Int> {
        val filtered = enabledMap.filter { it.value }

        return filtered.keys.toArrayList()
    }

    public fun setSelected(selected: List<Int>) {
        selected.forEach {
            Timber.d("%d", it)
            enabledMap.put(it, true)
        }

    }

    public fun addSensor(sensor: WearableSensor) {
        sensorList.add(sensor)
        notifyDataSetChanged()

        if (!enabledMap.containsKey(sensor.type))
            enabledMap.put(sensor.type, false)
    }

    public fun clear() {
        sensorList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val sensor = sensorList[position]
        val enabled = enabledMap[sensor.type]

        holder?.checkBox?.isChecked = enabled
        holder?.checkBox?.text = sensor.name
        holder?.checkBox?.tag = sensor.type
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder?
            = ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_sensor, parent, false))

    override fun getItemCount(): Int = sensorList.size

    inner class ViewHolder : RecyclerView.ViewHolder {
        val checkBox by bindView<CheckBox>(R.id.checkbox)

        constructor(itemView: View?) : super(itemView) {
            checkBox.clicks().subscribe { enabledMap.put(checkBox.tag as Int, checkBox.isChecked) }
        }
    }
}