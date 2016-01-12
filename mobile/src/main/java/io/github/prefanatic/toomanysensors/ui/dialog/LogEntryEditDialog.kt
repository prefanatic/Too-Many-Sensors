package io.github.prefanatic.toomanysensors.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.Category
import io.github.prefanatic.toomanysensors.data.realm.LogEntry
import io.realm.Realm
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.*

class LogEntryEditDialog : DialogFragment() {
    lateinit var name: EditText
    lateinit var category: Spinner
    lateinit var addCategory: ImageButton
    private var categoryData: ArrayList<String> = ArrayList()
    private var logEntry: LogEntry? = null
    private val subject: PublishSubject<LogEntry> = PublishSubject.create()

    companion object {
        public fun newInstance(entry: LogEntry): LogEntryEditDialog {
            val dialog = LogEntryEditDialog()
            val bundle = Bundle()

            bundle.putLong("dateRecorded", entry.dateCollected)
            bundle.putLong("length", entry.lengthOfCollection)

            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_log_entry_edit, null)

        name = view.findViewById(R.id.entry_name) as EditText
        category = view.findViewById(R.id.entry_category) as Spinner
        addCategory = view.findViewById(R.id.add_category) as ImageButton

        populateCategorySpinner()

        addCategory.setOnClickListener {
            val editDialog = CategoryEditDialog.newInstance("")
            editDialog.getResultObservable().subscribe { // We can probably use Kotlin magic here instead of RxJava to do this.
                populateCategorySpinner()
            }

            editDialog.show(fragmentManager, "categoryEdit")
        }

        Realm.getInstance(activity).use {
            val entry = it.where(LogEntry::class.java)
                    .equalTo("dateCollected", arguments.getLong("dateRecorded"))
                    .equalTo("lengthOfCollection", arguments.getLong("length"))
                    .findFirst() // Should we do this async?

            if (entry != null) {
                name.setText(entry.name)
                category.setSelection(categoryData.indexOf(entry.category))

                logEntry = it.copyFromRealm(entry)
            } else {

            }
        }

        dialog.setView(view)
                .setPositiveButton(R.string.edit_positive, { dialog, i -> onPositiveClicked() })
                .setNeutralButton(R.string.edit_neutral, { dialog, i -> dismiss() })
                .setTitle(R.string.edit_title)

        return dialog.create()
    }

    public fun getResultObservable() = subject.asObservable()

    private fun populateCategorySpinner() {
        Realm.getInstance(activity).use {
            val categories = it.allObjects(Category::class.java)

            if (categories.size == 0) {
                categoryData.add("None")
            } else
                categories.forEach { categoryData.add(it.name) }

            category.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, categoryData)
        }
    }

    private fun onPositiveClicked() {
        if (logEntry != null) {
            logEntry?.name = name.text.toString()
            logEntry?.category = category.selectedItem as String

            Timber.d("Log Entry (%s) collected on %d", logEntry!!.name, logEntry!!.dateCollected)

            Realm.getInstance(activity).use {
                it.executeTransaction { it.copyToRealmOrUpdate(logEntry) }
            }

            subject.onNext(logEntry)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        subject.onCompleted()
        super.onDismiss(dialog)
    }
}