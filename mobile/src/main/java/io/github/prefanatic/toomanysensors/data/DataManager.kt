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

package io.github.prefanatic.toomanysensors.data

import android.content.Context
import android.hardware.SensorManager
import io.github.prefanatic.toomanysensors.data.dto.SensorData
import io.github.prefanatic.toomanysensors.data.realm.LogData
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.data.realm.LogValue
import io.github.prefanatic.toomanysensors.data.realm.RealmFloatWrapper
import io.realm.Realm
import timber.log.Timber
import java.util.*

public class DataManager private constructor() {
    private val sensorMap: HashMap<Int, LogData>
    private var inTransaction: Boolean
    private var sensorIds: IntArray? = null
    private var logEntry: LogEntry? = null

    init {
        sensorMap = HashMap();
        inTransaction = false
    }

    companion object {
        private val INSTANCE = DataManager()

        public fun get() = INSTANCE
    }

    public fun isInTransaction() = inTransaction

    public fun addData(data: SensorData) {
        if (!isInTransaction()) {
            throw RuntimeException("Attempting to add data while not in a transaction!")
        }

        val logData = sensorMap[data.sensor]
        val logValue = LogValue()

        logValue.timeStamp = data.timestamp;

        data.values.forEach {
            logValue.values.add(RealmFloatWrapper(it))
        }

        logData?.entries?.add(logValue)
    }

    public fun setSensorIds(sensorIds: IntArray) {
        this.sensorIds = sensorIds
    }

    public fun beginTransaction(context: Context) {
        if (isInTransaction()) {
            throw RuntimeException("Attempting to start a transaction when we're already in one!")
        }
        if (sensorIds == null) {
            throw RuntimeException("No sensor IDs have been set to save in this transaction!")
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        logEntry = LogEntry()
        logEntry?.dateCollected = System.currentTimeMillis()

        (sensorIds as IntArray).forEach {
            val data = LogData()
            data.sensorId = it
            data.sensorName = sensorManager.getDefaultSensor(it).name

            sensorMap[it] = data
        }

        inTransaction = true
    }

    public fun endTransaction(context: Context) {
        logEntry?.lengthOfCollection = System.currentTimeMillis() - logEntry!!.dateCollected

        Realm.getInstance(context).use {
            it.executeTransaction {
                val bgRealm = it

                Timber.d("Saving %d sensors.", sensorMap.entries.size)

                sensorMap.forEach { it ->
                    logEntry?.data?.add(it.value)
                    Timber.d("Saving sensor (%d) with %d values.", it.key, it.value.entries.size)
                }


                bgRealm.copyToRealm(logEntry)

                sensorMap.clear()
                Timber.d("Transaction executed.")
            }
        }

        inTransaction = false
        sensorIds = null
    }
}