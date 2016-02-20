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

package io.github.prefanatic.toomanysensors.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.design.widget.Snackbar
import edu.uri.egr.hermes.manipulators.FileLog
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
        val realm = Realm.getInstance(activity)
        val config = realm.configuration
        realm.close()

        Realm.deleteRealm(config)

        Snackbar.make(view, "Realm reset.", Snackbar.LENGTH_LONG)
                .show()

        return true
    }

    private fun exportRealm(): Boolean {
        var file = File(activity.externalCacheDir, "export.realm")
        file.delete()

        Realm.getInstance(activity).use {
            try {
                it.writeCopyTo(file)
            } catch (e: IOException) {
                Timber.e(e, "Failed to export Realm database.")
                Snackbar.make(view, e.message as CharSequence, Snackbar.LENGTH_INDEFINITE)
                return false
            }
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.apply {
            type = "plain/text"
            putExtra(Intent.EXTRA_EMAIL, "joncgoldberg@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "TooManySensors - Realm Export")
            putExtra(Intent.EXTRA_TEXT, "Exported on %s".format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))))
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        }

        startActivity(Intent.createChooser(intent, "Export Realm"))
        return true
    }
}