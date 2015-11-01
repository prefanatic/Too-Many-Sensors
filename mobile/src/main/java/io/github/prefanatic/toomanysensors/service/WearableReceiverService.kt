package io.github.prefanatic.toomanysensors.service

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.wearable.Channel
import edu.uri.egr.hermes.Hermes
import edu.uri.egr.hermeswear.HermesWearable
import edu.uri.egr.hermeswear.event.ChannelEvent
import io.github.prefanatic.toomanysensors.extension.BUFFER_SIZE
import io.github.prefanatic.toomanysensors.extension.PATH_TRANSFER_DATA
import io.github.prefanatic.toomanysensors.data.SensorData
import io.github.prefanatic.toomanysensors.manager.SensorDataBus
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

public class WearableReceiverService : IntentService("WearableReceiverService") {
    private fun handleInputStream(stream: InputStream) {
        var bytesRead: Int
        val buffer = ByteArray(BUFFER_SIZE)

        stream.use {
            while (true) {
                bytesRead = it.read(buffer, 0, BUFFER_SIZE)
                if (bytesRead == -1)
                    break

                val data = ByteBuffer.wrap(buffer)
                val sensor = data.int
                val time = data.long
                val values = FloatArray(data.int)

                for (i in 0..values.size - 1) {
                    values[i] = data.float
                }

                SensorDataBus.post(SensorData(sensor, time, values))
            }
        }
    }

    private fun handleChannelOpened(channel: Channel) {
        HermesWearable.Channel.getInputStream(channel)
                .subscribeOn(Schedulers.io())
                .subscribe { stream -> handleInputStream(stream) }    }


    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val event = intent.getParcelableExtra<ChannelEvent>(Hermes.EXTRA_OBJECT)
        if (event.channel.path.equals(PATH_TRANSFER_DATA))
            handleChannelOpened(event.channel)
    }
}
