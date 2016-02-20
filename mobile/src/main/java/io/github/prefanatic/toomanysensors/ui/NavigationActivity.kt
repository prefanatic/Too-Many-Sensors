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

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.extension.STATE_NAV
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.extension.setStatusBarColor
import io.github.prefanatic.toomanysensors.extension.showFragment
import io.github.prefanatic.toomanysensors.ui.fragment.ObserveFragment
import io.github.prefanatic.toomanysensors.ui.fragment.RecallFragment
import io.github.prefanatic.toomanysensors.ui.fragment.SettingsFragment

class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val toolbar by bindView<Toolbar>(R.id.toolbar)
    val drawerLayout by bindView<DrawerLayout>(R.id.drawer_layout)
    val navView by bindView<NavigationView>(R.id.navigation_view)

    var currentNavItem: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        setSupportActionBar(toolbar)
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        with (supportActionBar!!) {
            setHomeAsUpIndicator(R.drawable.ic_menu_24dp)
            setDisplayHomeAsUpEnabled(true)
        }

        navView.setNavigationItemSelectedListener(this)
        handleNavigationSwitch(if (currentNavItem == -1) R.id.observe else currentNavItem)
    }

    private fun handleNavigationSwitch(id: Int) {
        currentNavItem = id
        navView.menu.findItem(id).setChecked(true)

        when (id) {
            R.id.observe -> showFragment(R.id.content, ::ObserveFragment)
            R.id.recall -> showFragment(R.id.content,  ::RecallFragment)
            R.id.settings -> showFragment(R.id.content, ::SettingsFragment)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem?): Boolean {
        item?.setChecked(true)
        drawerLayout.closeDrawer(GravityCompat.START)

        handleNavigationSwitch(item!!.itemId)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        currentNavItem = savedInstanceState?.getInt(STATE_NAV) ?: -1

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putInt(STATE_NAV, currentNavItem)
        }

        super.onSaveInstanceState(outState)
    }
}