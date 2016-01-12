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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.adapter.LogDataAdapter
import io.github.prefanatic.toomanysensors.extension.bindView
import io.github.prefanatic.toomanysensors.extension.showFragment
import io.github.prefanatic.toomanysensors.ui.LogEntryActivity
import timber.log.Timber

class LogDataListFragment : Fragment() {
    val recycler by bindView<RecyclerView>(R.id.recycler)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater?.inflate(R.layout.fragment_log_entry, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = getLogEntry()?.data
        val adapter = LogDataAdapter(data?.subList(0, data.size)!!)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(activity)

        adapter.getClickObservable().subscribe {
            Timber.d("Clicked on %s", it.obj.sensorName)

            (activity as LogEntryActivity).showFragment(R.id.content, LogDataGraphFragment.newInstance(it.viewHolder.adapterPosition))
        }
    }

    private fun getLogEntry() = (activity as LogEntryActivity).logEntry
}