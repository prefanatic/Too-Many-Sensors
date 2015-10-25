package io.github.prefanatic.toomanysensors

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder
import android.os.Parcelable
import com.google.android.gms.wearable.MessageEvent
import edu.uri.egr.hermes.Hermes
import edu.uri.egr.hermeswear.HermesWearable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

class SensorService : Service() {
    val mSubscriptions = CompositeSubscription()
    var mOutputStream: OutputStream? = null

    private fun observeSensor(sensor: Int, samplingRate: Int, maximumReport: Int) {
        val subscription = Hermes.Sensor.observeSensor(sensor, samplingRate, maximumReport)
                .subscribe {
                    val buffer = ByteBuffer.allocate(BUFFER_SIZE)

                    buffer.putInt(sensor)
                    buffer.putLong(System.currentTimeMillis())
                    buffer.putInt(it.values.size)

                    it.values.forEach { buffer.putFloat(it) }

                    try {
                        mOutputStream?.write(buffer.array())
                    } catch (e: IOException) {
                        Timber.e(e, "Failed to send sensor data.")
                        // We probably don't want to stop self here, cause other sensors still may be workin fine.
                    }
                }

        mSubscriptions.add(subscription)
    }

    private fun sendSensors(stream: OutputStream) {
        val outputStream = DataOutputStream(stream)
        val manager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorList = manager.getSensorList(Sensor.TYPE_ALL)

        try {
            outputStream.writeInt(sensorList.size)

            sensorList.forEach {
                outputStream.writeInt(it.type)
                outputStream.writeUTF(it.name)
            }

            outputStream.close()
        } catch (e: IOException) {
            Timber.e(e, "Failed to send sensor list.")
        }

        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messageEvent = intent?.getParcelableExtra<Parcelable>(Hermes.EXTRA_OBJECT) as MessageEvent // wtf???

        when (messageEvent.path) {
            PATH_START -> {
                val buffer = ByteBuffer.wrap(messageEvent.data)
                val samplingRate = buffer.int
                val maximumReport = buffer.int
                val sensors = IntArray(buffer.int)

                for (i in 0..sensors.size - 1)
                    sensors[i] = buffer.int

                HermesWearable.Channel.openOutputStream(messageEvent.sourceNodeId, PATH_TRANSFER_DATA)
                        .doOnError {
                            Timber.e(it, "Failed to open output stream.")
                            stopSelf()
                        }
                        .subscribe {
                            mOutputStream = it
                            sensors.forEach { observeSensor(it, samplingRate, maximumReport) }
                        }
            }
            PATH_STOP -> {
                clean()
                stopSelf()
            }
            PATH_REQUEST_SENSORS -> HermesWearable.Channel.openOutputStream(messageEvent.sourceNodeId, PATH_TRANSFER_SENSOR_LIST).subscribe { sendSensors(it) }
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        clean()
        super.onDestroy()
    }

    private fun clean() {
        if (!mSubscriptions.isUnsubscribed)
            mSubscriptions.unsubscribe()

        if (mOutputStream != null) {
            try { mOutputStream?.close() } catch (e: IOException) {}
            mOutputStream = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException()
    }
}