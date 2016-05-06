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
import android.content.Context
import android.content.Intent
import android.net.Uri
import edu.uri.egr.hermes.Hermes
import edu.uri.egr.hermes.manipulators.FileLog
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.realm.Realm

import rx.Observable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exports a Realm LogEntry based off of the timestamp - then returns a result over Hermes.Dispatch.
 */
class LogEntryExportService : IntentService("LogEntryExportService") {

    override fun onHandleIntent(intent: Intent) {
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0L)
        if (timestamp == 0L) return

        val subject = Hermes.Dispatch.getSubject<List<Uri>>(DISPATCH)
        val realm = Realm.getInstance(this)
        val logEntry = realm.where(LogEntry::class.java)
                .equalTo("dateCollected", timestamp)
                .findFirst()

        if (logEntry == null) {
            subject.onError(RuntimeException("Unable to locate LogEntry."))
            subject.onCompleted()
            return
        }

        val title = "${logEntry.name} - ${getDateCollected(logEntry).replace("/", ".").replace(":", ".")}"
        val uriList = ArrayList<Uri>()

        // Open up a .csv to export generic data to.
        val export = FileLog("$title - overview.csv")
        export.apply {
            setHeaders("Name", "Category", "Sensors Collected", "Length of Collection (ms)", "Notes")
            write(logEntry.name, logEntry.category, logEntry.data?.joinToString { it.sensorName }, logEntry.lengthOfCollection, logEntry.notes)

            uriList.add(Uri.fromFile(file))
        }

        // Loop through our LogData sources and write individualized files for those.
        logEntry.data.forEach {
            val entry = FileLog("$title - ${it.sensorName}.csv")
            entry.setHeaders("Timestamp (ms)", *(0..it.entries[0].values.size - 1).map { "Channel ${it.toString()}" }.toTypedArray())

            // Loop through our entries and add them to the file.
            for (value in it.entries) {
                entry.writeSpecific(0, value.timeStamp)

                value.values.forEachIndexed { i, wrapper ->
                    entry.writeSpecific(i + 1, wrapper.value)
                }

                entry.flush()
            }

            uriList.add(Uri.fromFile(entry.file))
        }

        // Push it out to dispatch!
        subject.onNext(uriList)
        subject.onCompleted()
    }

    private fun getDateCollected(logEntry: LogEntry) = SimpleDateFormat("h:mm a MM/dd/yy").format(Date(logEntry.dateCollected))

    companion object {
        const val EXTRA_TIMESTAMP = "timestamp"
        const val DISPATCH = "log.entry.export.service.dispatch"

        fun exportEntry(context: Context, timestamp: Long): Observable<List<Uri>> {
            val intent = Intent(context, LogEntryExportService::class.java)

            // Create our dispatch subject.
            Hermes.Dispatch.createSubject<List<Uri>>(DISPATCH)

            intent.putExtra(EXTRA_TIMESTAMP, timestamp)
            context.startService(intent)

            return Hermes.Dispatch.getObservable(DISPATCH)
        }
    }
}
