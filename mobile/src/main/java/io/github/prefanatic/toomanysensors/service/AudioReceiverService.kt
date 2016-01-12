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