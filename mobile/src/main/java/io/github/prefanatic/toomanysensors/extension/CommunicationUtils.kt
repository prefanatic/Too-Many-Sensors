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

package io.github.prefanatic.toomanysensors.extension

import android.hardware.SensorManager
import edu.uri.egr.hermeswear.HermesWearable
import io.github.prefanatic.toomanysensors.data.dto.WearableSensor
import rx.Observable
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

/**
 * io.github.prefanatic.toomanysensors.manager (Cody Goldberg - 3/30/2016)
 */

fun sendStartRequest(id: String, sensors: List<WearableSensor>) {
    val buffer = ByteBuffer.allocate(12 + (4 * sensors.size))

    buffer.putInt(SensorManager.SENSOR_DELAY_NORMAL)
    buffer.putInt(0)
    buffer.putInt(sensors.size)

    sensors.forEach {
        buffer.putInt(it.type)
    }

    HermesWearable.Message.sendMessage(id, PATH_START, buffer.array())
            .subscribe()
}

fun sendStopRequest(id: String) {
    HermesWearable.Message.sendMessage(id, PATH_STOP)
            .subscribe()
}

fun readSensorsFromWearable(id: String): Observable<WearableSensor> {
    val observable = HermesWearable.getChannelOpened()
            .filter { it.channel.path.equals(PATH_TRANSFER_SENSOR_LIST) }
            .first()
            .flatMap { HermesWearable.Channel.getInputStream(it.channel) }
            .flatMap { readSensorList(it) }

    HermesWearable.Message.sendMessage(id, PATH_REQUEST_SENSORS)
            .subscribe { }

    return observable
}

private fun readSensorList(stream: InputStream): Observable<WearableSensor> {
    val sensors = ArrayList<WearableSensor>()

    DataInputStream(stream).use {
        try {
            for (i in 1..it.readInt()) {
                val type = it.readInt();
                val name = it.readUTF();

                sensors.add(WearableSensor(name, type))
            }
        } catch (e: EOFException) {
            // Do nothing - this is intentional!
        }
    }

    return Observable.just(sensors)
            .flatMapIterable { it }
}