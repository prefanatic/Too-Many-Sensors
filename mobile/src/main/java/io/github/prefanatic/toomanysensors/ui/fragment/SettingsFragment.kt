package io.github.prefanatic.toomanysensors.ui.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import io.github.prefanatic.toomanysensors.R

class SettingsFragment: PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
    }
}