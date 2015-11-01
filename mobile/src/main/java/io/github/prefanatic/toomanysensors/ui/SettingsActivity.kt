package io.github.prefanatic.toomanysensors.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.extension.showFragment
import io.github.prefanatic.toomanysensors.ui.fragment.SettingsFragment

public class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        with(supportActionBar) {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
            setDisplayHomeAsUpEnabled(true)
        }

        showFragment(android.R.id.content, ::SettingsFragment)
    }
}
