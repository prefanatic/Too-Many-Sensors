package io.github.prefanatic.toomanysensors.data

import android.content.Context
import io.github.prefanatic.toomanysensors.data.dto.SensorData
import io.github.prefanatic.toomanysensors.data.realm.LogData
import io.github.prefanatic.toomanysensors.data.realm.LogValue
import io.github.prefanatic.toomanysensors.data.realm.RealmFloatWrapper
import io.realm.Realm
import io.realm.RealmList
import timber.log.Timber
import java.util.*

public class DataManager private constructor() {
    private val sensorMap: HashMap<Int, LogData>
    private var inTransaction: Boolean
    private var sensorIds: IntArray? = null

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

    public fun beginTransaction() {
        if (isInTransaction()) {
            throw RuntimeException("Attempting to start a transaction when we're already in one!")
        }
        if (sensorIds == null) {
            throw RuntimeException("No sensor IDs have been set to save in this transaction!")
        }

        (sensorIds as IntArray).forEach {
            val data = LogData()

            data.sensorId = it
            data.dateCollected = System.currentTimeMillis()

            sensorMap[it] = data
        }

        inTransaction = true
    }

    public fun endTransaction(context: Context) {
        val realm = Realm.getInstance(context)

        realm.executeTransaction {
            val bgRealm = it

            Timber.d("Saving %d sensors.", sensorMap.entries.size)
            sensorMap.forEach {
                Timber.d("Saving sensor (%d) with %d values.", it.key, it.value.entries.size)

                bgRealm.copyToRealm(it.value)
            }

            sensorMap.clear()
            Timber.d("Transaction executed.")
        }

        inTransaction = false
        sensorIds = null
    }
}