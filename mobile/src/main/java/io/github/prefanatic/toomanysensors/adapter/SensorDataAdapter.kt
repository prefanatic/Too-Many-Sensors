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

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.LIVE_GRAPH_ENABLED
import io.github.prefanatic.toomanysensors.data.dto.SensorData
import io.github.prefanatic.toomanysensors.data.dto.WearableSensor
import io.github.prefanatic.toomanysensors.extension.bindView
import java.util.*

class SensorDataAdapter(val context: Context) : RecyclerView.Adapter<SensorDataAdapter.ViewHolder>() {
    public val sensorList = ArrayList<WearableSensor>()
    private val chartDataMap = HashMap<Int, BarData>()
    private val maximumValueMap = HashMap<Int, Float>()
    private val minimumValueMap = HashMap<Int, Float>()
    private var recyclerView: RecyclerView? = null
    private val showGraph: Boolean

    init {
        showGraph = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(LIVE_GRAPH_ENABLED, true)
    }

    public fun setSensors(sensors: List<WearableSensor>) {
        sensorList.clear()
        chartDataMap.clear()
        sensors.forEach { sensorList.add(it) }
    }

    public fun updateSensorData(sensorData: SensorData) {
        if (!showGraph) return

        val sensorInListIndex = getSensorIndex(sensorData)

        if (!chartDataMap.containsKey(sensorInListIndex)) {
            chartDataMap.put(sensorInListIndex, initializeBarChart(sensorData.values.size))
        } else {
            val data = chartDataMap[sensorInListIndex]
            val dataSet = data!!.dataSets[0]

            maximumValueMap.put(sensorInListIndex, sensorData.values.max()!! + 10)
            minimumValueMap.put(sensorInListIndex, sensorData.values.min()!! - 10)
            sensorData.values.forEachIndexed { i, value ->
                dataSet.yVals[i].`val` = value
            }
        }

        notifyItemChanged(sensorInListIndex)
    }

    private fun getSensorIndex(sensorData: SensorData): Int {
        sensorList.forEachIndexed { i, wearableSensor ->
            if (wearableSensor.type == sensorData.sensor)
                return i
        }

        return -1
    }

    private fun initializeBarChart(valueCount: Int): BarData {
        val xValues = ArrayList<String>(valueCount)
        val yValues = ArrayList<BarEntry>(valueCount)

        for (i in 0..valueCount - 1) {
            xValues.add(i.toString())
            yValues.add(BarEntry(0.toFloat(), i))
        }

        val dataSet = BarDataSet(yValues, "DataSet")
        dataSet.color = ContextCompat.getColor(context, R.color.colorAccent)

        return BarData(xValues, dataSet)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val data = sensorList[position]

        if (holder?.name?.text!!.equals(data.name) && holder?.chart?.data != null) {
            holder?.chart?.apply {
                axisLeft?.axisMaxValue = maximumValueMap[position]!!
                axisLeft?.axisMinValue = minimumValueMap[position]!!
                notifyDataSetChanged()
                invalidate()
            }
        } else {
            holder?.name?.text = data.name
            holder?.chart?.data = chartDataMap[position]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_sensor_data, parent, false))

    override fun getItemCount() = sensorList.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = null
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        val name by bindView<TextView>(R.id.sensor_name)
        val chart by bindView<BarChart>(R.id.chart)

        constructor(itemView: View?) : super(itemView) {
            if (!showGraph) {
                chart.visibility = View.GONE
            } else {
                with(chart) {
                    setTouchEnabled(false)
                    isDragEnabled = false
                    setDescription("")
                    setDrawGridBackground(false)

                    legend.isEnabled = false
                    xAxis.setDrawGridLines(false)
                    axisRight.isEnabled = false
                }
            }
        }
    }
}