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
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar
import com.tbruyelle.rxpermissions.RxPermissions
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.extension.showFragment
import io.github.prefanatic.toomanysensors.extension.showSnackbar
import io.github.prefanatic.toomanysensors.service.LogEntryExportService
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
    lateinit var realm: Realm;

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
                        dialog.resultObservable.subscribe {
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
            realm = Realm.getInstance(this);

            logEntry = realm.where(LogEntry::class.java)
                    .equalTo("dateCollected", timeStamp)
                    .equalTo("lengthOfCollection", length)
                    .findFirst() // Should we do this async?

            Timber.d("Loaded log entry: %s", logEntry?.name)

        }

        if (logEntry != null) {
            populateInformation()
            showFragment(R.id.content, ::LogDataListFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close();
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

        val progress = ProgressBar(this)
        progress.setPadding(0, 16, 0, 16)

        val builder = AlertDialog.Builder(this)
                .setTitle("Preparing Export")
                .setCancelable(false)
                .setView(progress)

        val dialog = builder.create()
        dialog.show()

        LogEntryExportService.exportEntry(this, (logEntry as LogEntry).dateCollected)
                .subscribe({
                    dialog.hide()

                    // Push this though an intent!
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                    intent.apply {
                        type = "plain/text"
                        putExtra(Intent.EXTRA_SUBJECT, "TooManySensors - CSV Export")
                        putExtra(Intent.EXTRA_TEXT, "Exported on %s".format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))))
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(it))
                    }

                    startActivity(Intent.createChooser(intent, "Export CSV"))
                }, {
                    showSnackbar(it.message!!)
                })
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else
            super.onBackPressed()
    }
}