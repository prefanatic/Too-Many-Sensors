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
import java.util.*

class SensorAdapter : RecyclerView.Adapter<SensorAdapter.ViewHolder>() {
    public val sensorList = ArrayList<WearableSensor>()
    private val enabledMap = HashMap<Int, Boolean>()

    public fun getSelected(): ArrayList<Int> {
        val filtered = enabledMap.filter { it.value }

        return filtered.keys.toArrayList()
    }

    public fun setSelected(selected: ArrayList<Int>) {
        selected.forEach { enabledMap.put(it, true) }
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