package io.github.prefanatic.toomanysensors.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.extension.showFragment
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
            val dateCollected = SimpleDateFormat("HH:mm MM/dd/yy").format(Date((logEntry as LogEntry).dateCollected))
            val cal = Calendar.getInstance()
            cal.timeInMillis = (logEntry as LogEntry).lengthOfCollection

            val lengthCollected = SimpleDateFormat("HH:mm:ss").format(cal.time)

            dateOfCollectionText.text = "Recorded at %s".format(dateCollected)
            lengthOfCollectionText.text = "Duration: %s".format(lengthCollected)
            toolbar.title = (logEntry as LogEntry).name

            showFragment(R.id.content, ::LogDataListFragment)
        }
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else
            super.onBackPressed()
    }
}