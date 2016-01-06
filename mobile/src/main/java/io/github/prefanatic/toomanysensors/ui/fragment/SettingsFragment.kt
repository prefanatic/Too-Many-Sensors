package io.github.prefanatic.toomanysensors.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.support.design.widget.Snackbar
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.EXPORT_REALM
import io.github.prefanatic.toomanysensors.data.RESET_REALM
import io.realm.Realm
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment: PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        preferenceScreen.findPreference(EXPORT_REALM).setOnPreferenceClickListener {
            exportRealm()
        }
        preferenceScreen.findPreference(RESET_REALM).setOnPreferenceClickListener {
            resetRealm()
        }
    }

    private fun resetRealm(): Boolean {
        val realm = Realm.getInstance(context)
        val config = realm.configuration
        realm.close()

        Realm.deleteRealm(config)

        Snackbar.make(view, "Realm reset.", Snackbar.LENGTH_LONG)
                .show()

        return true
    }

    private fun exportRealm(): Boolean {
        val realm = Realm.getInstance(context)
        var file: File? = null

        try {
            file = File(context.externalCacheDir, "export.realm")
            file.delete()

            realm.writeCopyTo(file)
        } catch (e: IOException) {
            Timber.e(e, "Failed to export Realm database.")
            Snackbar.make(view, e.message, Snackbar.LENGTH_INDEFINITE)
            return false
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.apply {
            setType("plain/text")
            putExtra(Intent.EXTRA_EMAIL, "dumbplanet424@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "TooManySensors - Realm Export")
            putExtra(Intent.EXTRA_TEXT, "Exported on %s".format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))))
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        }

        startActivity(Intent.createChooser(intent, "Export Realm"))
        return true
    }
}