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
import android.net.Uri

import rx.Observable

/**
 * Exports a Realm LogEntry based off of the timestamp - then returns a result over Hermes.Dispatch.
 */
class LogEntryExportService : IntentService("LogEntryExportService") {

    override fun onHandleIntent(intent: Intent) {

    }

    companion object {
        private val EXTRA_TIMESTAMP = "timestamp"

        fun exportEntry(context: Context, timestamp: Long): Observable<Uri> {
            val intent = Intent()
        }
    }
}
