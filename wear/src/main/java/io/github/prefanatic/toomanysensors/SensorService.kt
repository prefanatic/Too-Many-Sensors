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

package io.github.prefanatic.toomanysensors

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder
import android.os.Parcelable
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
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
    lateinit var sourceNodeId: String

    private fun cancelNotification() {
        NotificationManagerCompat.from(this).cancel(100);
    }

    private fun generateNotification() {
        val builder = NotificationCompat.Builder(this)

        builder.setContentTitle("Too Many Sensors")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Observing sensor data")
                .setOngoing(true)


        NotificationManagerCompat.from(this).notify(100, builder.build())
    }

    private fun observeSensor(sensor: Int, samplingRate: Int, maximumReport: Int) {
        if (sensor == 1001) {
            // Are we requesting audio??

        } else {
            val subscription = Hermes.Sensor.observeSensor(sensor, samplingRate, maximumReport)
                    .subscribe({
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
                    }, { e ->
                        Timber.e(e, "Failed to initialize sensor.")

                        // Notify the listening device about this sadness.
                        HermesWearable.Message.sendMessage(sourceNodeId, PATH_ERROR, e.message?.toByteArray()).subscribe()
                    })

            mSubscriptions.add(subscription)
        }
    }

    private fun sendSensors(stream: OutputStream) {
        val outputStream = DataOutputStream(stream)
        val manager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorList = manager.getSensorList(Sensor.TYPE_ALL)

        outputStream.use {
            it.writeInt(sensorList.size + 1)

            sensorList.forEach {
                outputStream.writeInt(it.type)
                outputStream.writeUTF(it.name)
            }

            // We need to hardcode out the Heart Rate sensor.  It doesn't seem to be included in Sensor.TYPE_ALL
            val heartRate = manager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            Timber.d("Heart rate: %s", heartRate);
            if (heartRate != null) {
                outputStream.writeInt(heartRate.type)
                outputStream.writeUTF(heartRate.name)
            }

            // Tell them we do audio as well.
            //outputStream.writeInt(1001)
            //outputStream.writeUTF("Microphone")
        }

        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messageEvent = intent?.getParcelableExtra<Parcelable>(Hermes.EXTRA_OBJECT) as MessageEvent // wtf???
        sourceNodeId = messageEvent.sourceNodeId

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

                generateNotification()
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
        cancelNotification()

        if (!mSubscriptions.isUnsubscribed)
            mSubscriptions.unsubscribe()

        if (mOutputStream != null) {
            try {
                mOutputStream?.close()
            } catch (e: IOException) {
            }
            mOutputStream = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException()
    }
}