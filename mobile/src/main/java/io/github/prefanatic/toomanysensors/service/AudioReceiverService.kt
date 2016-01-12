package io.github.prefanatic.toomanysensors.service

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.wearable.Channel
import edu.uri.egr.hermes.Hermes
import edu.uri.egr.hermeswear.HermesWearable
import edu.uri.egr.hermeswear.event.ChannelEvent
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream

class AudioReceiverService : IntentService("AudioReceiverService") {

    private fun handleChannelOpened(channel: Channel) {
        try {
            val outputFile = Hermes.File.create("toomanysensors_audio.raw")
            val bufferedFileOutputStream = BufferedOutputStream(FileOutputStream(outputFile))
            val inputStream = HermesWearable.Channel.getInputStream(channel)
                    .toBlocking()
                    .first()
            val bufferedInputStream = BufferedInputStream(inputStream)
            var bytesRead = 0
            val buffer = ByteArray(1024)

            while (bytesRead != 1) {
                bytesRead = bufferedInputStream.read(buffer)

                for (i in 0..bytesRead) {
                    bufferedFileOutputStream.write(buffer[i].toInt())
                }
            }

            bufferedInputStream.close()
            bufferedFileOutputStream.close()



        } catch (e: Exception) {
            Timber.e(e, "Failed to receive audio")
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val event = intent.getParcelableExtra<ChannelEvent>(Hermes.EXTRA_OBJECT)

            if (event.channel.path.equals("audio"))
                handleChannelOpened(event.channel)
        }
    }
}