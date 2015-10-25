package io.github.prefanatic.toomanysensors

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.wearable.Channel
import edu.uri.egr.hermes.Hermes
import edu.uri.egr.hermeswear.HermesWearable
import edu.uri.egr.hermeswear.event.ChannelEvent
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

public class WearableReceiverService : IntentService("WearableReceiverService") {
    private fun handleInputStream(stream: InputStream) {
        var bytesRead: Int
        val buffer = ByteArray(BUFFER_SIZE)

        try {
            while (true) {
                bytesRead = stream.read(buffer, 0, BUFFER_SIZE)
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
        } catch (e: IOException) {
            Timber.e(e, "Failed to read sensor data.")
        }
    }

    private fun handleChannelOpened(channel: Channel) {
        HermesWearable.Channel.getInputStream(channel)
                .subscribeOn(Schedulers.io())
                .subscribe { stream -> handleInputStream(stream) }
    }


    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val event = intent.getParcelableExtra<ChannelEvent>(Hermes.EXTRA_OBJECT)
        if (event.channel.path.equals(PATH_TRANSFER_DATA))
            handleChannelOpened(event.channel)
    }
}
