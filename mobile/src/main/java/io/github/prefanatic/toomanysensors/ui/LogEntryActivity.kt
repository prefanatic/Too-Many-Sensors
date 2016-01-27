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

package io.github.prefanatic.toomanysensors.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar
import com.tbruyelle.rxpermissions.RxPermissions
import edu.uri.egr.hermes.manipulators.FileLog
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.extension.showFragment
import io.github.prefanatic.toomanysensors.ui.dialog.LogEntryEditDialog
import io.github.prefanatic.toomanysensors.ui.fragment.LogDataListFragment
import io.realm.Realm
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class LogEntryActivity : AppCompatActivity() {
    val toolbar by bindView<Toolbar>(R.id.toolbar)
    val dateOfCollectionText by bindView<TextView>(R.id.collection_date)
    val lengthOfCollectionText by bindView<TextView>(R.id.collection_length)
    var logEntry: LogEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_entry)

        toolbar.inflateMenu(R.menu.activity_log_entry)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)

        RxToolbar.navigationClicks(toolbar).subscribe { onBackPressed() }
        RxToolbar.itemClicks(toolbar).subscribe {
            when (it.itemId) {
                R.id.action_edit -> {
                    if (logEntry != null) {
                        val dialog = LogEntryEditDialog.newInstance(logEntry!!)
                        dialog.getResultObservable().subscribe {
                            logEntry = it
                            populateInformation()
                        }
                        dialog.show(fragmentManager, "entryEdit")
                    }
                }
            }
        }

        val permissionTrigger = RxToolbar.itemClicks(toolbar)
                .filter { it.itemId == R.id.action_export }


        RxPermissions.getInstance(this)
                .request(permissionTrigger, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { exportAsCsv() }

        val timeStamp = intent.getLongExtra("timeStamp", -1L)
        val length = intent.getLongExtra("length", -1L)
        Timber.d("Looking for %d -- %d", timeStamp, length)
        if (timeStamp != -1L && length != -1L) {
            Realm.getInstance(this).use {
                logEntry = it.where(LogEntry::class.java)
                        .equalTo("dateCollected", timeStamp)
                        .equalTo("lengthOfCollection", length)
                        .findFirst() // Should we do this async?

                logEntry = it.copyFromRealm(logEntry)

                Timber.d("Loaded log entry: %s", logEntry?.name)
            }
        }

        if (logEntry != null) {
            populateInformation()
            showFragment(R.id.content, ::LogDataListFragment)
        }
    }

    private fun getDateCollected() = SimpleDateFormat("h:mm a MM/dd/yy").format(Date((logEntry as LogEntry).dateCollected))

    private fun populateInformation() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = (logEntry as LogEntry).lengthOfCollection

        val lengthCollected = SimpleDateFormat("HH:mm:ss").format(cal.time)

        dateOfCollectionText.text = "Recorded at %s".format(getDateCollected())
        lengthOfCollectionText.text = "Duration: %s".format(lengthCollected)
        toolbar.title = (logEntry as LogEntry).name
    }

    private fun exportAsCsv() {
        if (logEntry == null) return

        val title = "${logEntry?.name} - ${getDateCollected().replace("/", ".").replace(":", ".")}"
        val uriList = ArrayList<Uri>()

        // Open up a .csv to export generic data to.
        val export = FileLog("$title - overview.csv")
        export.apply {
            setHeaders("Name", "Category", "Sensors Collected", "Length of Collection (ms)", "Notes")
            write(logEntry?.name, logEntry?.category, logEntry?.data?.joinToString { it.sensorName }, logEntry?.lengthOfCollection, logEntry?.notes)

            uriList.add(Uri.fromFile(file))
        }

        // Loop through our LogData sources and write individualized files for those.
        logEntry?.data?.forEach {
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

        // Push this though an intent!
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.apply {
            setType("plain/text")
            putExtra(Intent.EXTRA_EMAIL, "joncgoldberg@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "TooManySensors - CSV Export")
            putExtra(Intent.EXTRA_TEXT, "Exported on %s".format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))))
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        }

        startActivity(Intent.createChooser(intent, "Export CSV"))
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else
            super.onBackPressed()
    }
}