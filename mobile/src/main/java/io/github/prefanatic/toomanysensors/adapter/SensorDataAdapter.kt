package io.github.prefanatic.toomanysensors.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.SensorData
import io.github.prefanatic.toomanysensors.data.WearableSensor
import timber.log.Timber
import java.util.*

class SensorDataAdapter(val context: Context) : RecyclerView.Adapter<SensorDataAdapter.ViewHolder>() {
    public val sensorList = ArrayList<WearableSensor>()
    private val chartDataMap = HashMap<Int, BarData>()
    private val maximumValueMap = HashMap<Int, Float>()
    private var recyclerView: RecyclerView? = null

    public fun setSensors(sensors: ArrayList<WearableSensor>) {
        sensorList.clear()
        chartDataMap.clear()
        sensors.forEach { sensorList.add(it) }
    }

    public fun updateSensorData(sensorData: SensorData) {
        val sensorInListIndex = getSensorIndex(sensorData)

        if (!chartDataMap.containsKey(sensorInListIndex)) {
            chartDataMap.put(sensorInListIndex, initializeBarChart(sensorData.values.size))
        } else {
            val data = chartDataMap[sensorInListIndex]
            val dataSet = data!!.dataSets[0]

            maximumValueMap.put(sensorInListIndex, sensorData.values.max()!! + 10)
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

        Timber.d("We have %d values.", valueCount)
        for (i in 0..valueCount - 1) {
            xValues.add(i.toString())
            yValues.add(BarEntry(0.toFloat(), i))
        }

        val dataSet = BarDataSet(yValues, "DataSet")
        dataSet.color = context.getColor(R.color.colorAccent)

        return BarData(xValues, dataSet)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val data = sensorList[position]

        if (holder?.name?.text!!.equals(data.name) && holder?.chart?.data != null) {

            holder?.chart?.axisLeft?.axisMaxValue = maximumValueMap[position]
            holder?.chart?.notifyDataSetChanged()
            holder?.chart?.invalidate()
            //holder?.chart?.animateY(100)
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
            chart.setTouchEnabled(false)
            chart.isDragEnabled = false
            chart.legend.isEnabled = false
            chart.xAxis.setDrawGridLines(false)
            chart.setDescription("")
            chart.setDrawGridBackground(false)
            chart.axisRight.isEnabled = false
        }
    }
}