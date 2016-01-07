package io.github.prefanatic.toomanysensors.ui.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.adapter.RecallAdapter
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.github.prefanatic.toomanysensors.extension.bindView
import io.realm.Realm

class RecallFragment : Fragment() {
    val recycler by bindView<RecyclerView>(R.id.recycler)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_recall, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the LogEntry list we have from Realm.
        val realm = Realm.getInstance(context)
        val list = realm.allObjects(LogEntry::class.java)

        // Populate the recycler with our adapter.
        recycler.adapter = RecallAdapter(list)
        recycler.layoutManager = LinearLayoutManager(context)
    }
}