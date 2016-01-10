package io.github.prefanatic.toomanysensors.ui.fragment

import android.app.Fragment
import android.content.Intent
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
import io.github.prefanatic.toomanysensors.ui.LogEntryActivity
import io.realm.Realm
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

class RecallFragment : Fragment() {
    val recycler by bindView<RecyclerView>(R.id.recycler)
    val lifecycleSubscriptions = CompositeSubscription()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_recall, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the LogEntry list we have from Realm.
        val realm = Realm.getInstance(context)
        val list = realm.allObjects(LogEntry::class.java)

        // Populate the recycler with our adapter.
        val adapter = RecallAdapter(list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context)

        // Listen to clicks.
        lifecycleSubscriptions.add(
                adapter.getClickObservable().subscribe {
                    val obj = it.obj
                    val intent = Intent(activity, LogEntryActivity::class.java)

                    intent.apply {
                        putExtra("timeStamp", obj.dateCollected)
                        putExtra("length", obj.lengthOfCollection)
                    }

                    startActivity(intent)
                }
        )
    }
}