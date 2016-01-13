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

package io.github.prefanatic.toomanysensors.ui.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.ui.LogEntryActivity
import java.util.*

class LogDataGraphFragment : Fragment() {
    val chart by bindView<LineChart>(R.id.chart)


    companion object {
        public fun newInstance(sensor: Int): LogDataGraphFragment {
            val fragment = LogDataGraphFragment()
            val bundle = Bundle()

            bundle.apply {
                putInt("sensor", sensor)
            }

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater?.inflate(R.layout.fragment_log_graph, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create our array of colors for each channel.
        val colors = IntArray(5)
        colors[0] = resources.getColor(R.color.red, activity.theme)
        colors[1] = resources.getColor(R.color.purple, activity.theme)
        colors[2] = resources.getColor(R.color.indigo, activity.theme)
        colors[3] = resources.getColor(R.color.lightblue, activity.theme)
        colors[4] = resources.getColor(R.color.teal, activity.theme)

        val logData = getLogData()
        if (logData != null) {
            val channels = ArrayList<LineDataSet>()
            val xValues = ArrayList<String>()
            val timeStart = logData.entries[0].timeStamp

            // Populate the channels off of the first entry.
            logData.entries[0].values.forEachIndexed { i, v ->
                val dataSet = LineDataSet(ArrayList<Entry>(), "Channel $i")
                dataSet.apply {
                    color = colors[i]

                }
                channels.add(dataSet)
            }

            // Loop through each entry and channel value and populate.
            logData.entries.forEachIndexed { entryIndex, logValue ->
                //xValues.add((logValue.timeStamp - timeStart).toString())
                xValues.add(entryIndex.toString())

                logValue.values.forEachIndexed { i, v ->
                    channels[i].addEntry(Entry(v.value, entryIndex))
                }
            }

            // Combine the data in to the LineData object and populate the graph!
            val d = LineData(xValues, channels)
            chart.apply {
                data = d
                setDrawGridBackground(false)
                setDescription("")
                isAutoScaleMinMaxEnabled = true
            }
        }
    }

    private fun getLogData() = getLogEntry()!!.data[arguments.getInt("sensor")]
    private fun getLogEntry() = (activity as LogEntryActivity).logEntry
}
