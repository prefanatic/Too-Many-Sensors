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

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.clicks
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.adapter.ScheduleAdapter
import io.github.prefanatic.toomanysensors.data.realm.Schedule
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.ui.dialog.ScheduleEditDialog
import io.realm.Realm

/**
 * UI controller for scheduling sensors for periodic sampling.
 */
class ScheduleFragment : Fragment() {
    val recycler by bindView<RecyclerView>(R.id.recycler)
    val fab by bindView<FloatingActionButton>(R.id.fab)

    private lateinit var adapter: ScheduleAdapter
    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.clicks().subscribe { scheduleClicked() }

        adapter = ScheduleAdapter()
        adapter.getClickObservable()
                .subscribe { scheduleClicked(it.obj) }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(activity)

        // Get the schedules.
        realm = Realm.getDefaultInstance()

        val schedules = realm.where(Schedule::class.java).findAll();
        adapter.addAll(schedules)

        adapter.add(Schedule())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        realm.close()
    }

    private fun scheduleClicked(schedule: Schedule? = null) {
        val dialog = if (schedule == null) ScheduleEditDialog.newInstance() else ScheduleEditDialog.newInstance(schedule)

        dialog.resultObservable.subscribe { adapter.add(it) }
        dialog.show(fragmentManager, "scheduleEdit")
    }
}