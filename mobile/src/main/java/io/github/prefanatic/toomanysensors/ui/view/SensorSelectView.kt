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

package io.github.prefanatic.toomanysensors.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.FrameLayout
import io.github.prefanatic.toomanysensors.adapter.SensorAdapter
import io.github.prefanatic.toomanysensors.data.dto.WearableSensor

/**
 * View to encapsulate the sensor list.
 */
class SensorSelectView(context: Context) : FrameLayout(context) {
    private val recyclerView: RecyclerView
    private val sensorAdapter: SensorAdapter

    init {
        recyclerView = RecyclerView(context)
        sensorAdapter = SensorAdapter()

        recyclerView.apply {
            adapter = sensorAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    public fun addSensor(sensor: WearableSensor) =
            sensorAdapter.addSensor(sensor)

    public fun setSelected(selected: List<Int>) =
            sensorAdapter.setSelected(selected)

    public fun getSelected() =
            sensorAdapter.getSelected()

    public fun clear() =
            sensorAdapter.clear()
}