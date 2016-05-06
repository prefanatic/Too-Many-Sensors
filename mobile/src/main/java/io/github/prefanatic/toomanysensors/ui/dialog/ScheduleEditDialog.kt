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

package io.github.prefanatic.toomanysensors.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.widget.EditText
import com.jakewharton.rxbinding.widget.textChanges
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.Schedule
import io.realm.Realm
import org.threeten.bp.Instant
import rx.Observable

/**
 * io.github.prefanatic.toomanysensors.ui.dialog (Cody Goldberg - 4/10/2016)
 */

class ScheduleEditDialog : ObservableDialogFragment<Schedule>() {
    private var schedule: Schedule? = null
    override val result: Schedule?
        get() = schedule

    lateinit var name: EditText
    lateinit var periodicity: EditText
    lateinit var duration: EditText
    lateinit var randomize: EditText

    companion object {
        fun newInstance(schedule: Schedule): ScheduleEditDialog {
            val dialog = ScheduleEditDialog()
            val bundle = Bundle()

            bundle.putLong("epoch", schedule.generationEpoch)

            dialog.arguments = bundle
            return dialog
        }

        fun newInstance(): ScheduleEditDialog {
            return ScheduleEditDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_schedule_edit, null)
        name = view.findViewById(R.id.schedule_name) as EditText
        periodicity = view.findViewById(R.id.schedule_periodicity) as EditText
        duration = view.findViewById(R.id.schedule_duration) as EditText
        randomize = view.findViewById(R.id.schedule_randomize) as EditText

        dialog.setView(view)

        if (arguments != null) {
            Realm.getDefaultInstance().use {
                schedule = it.where(Schedule::class.java)
                        .equalTo("generationEpoch", arguments?.getLong("epoch", 0))
                        .findFirst()
            }
        }

        if (schedule != null && schedule is Schedule) {
            name.setText(schedule!!.name)
            periodicity.setText(schedule!!.peroidicity.toString())
            duration.setText(schedule!!.length.toString())
            randomize.setText(schedule!!.randomizePercent.toString())
        }

        // Rx Validation.
        val nameInputLayout = name.parent as TextInputLayout
        val periodInputLayout = periodicity.parent as TextInputLayout
        val durationInputLayout = duration.parent as TextInputLayout
        val randomInputLayout = randomize.parent as TextInputLayout

        nameInputLayout.error = "This schedule must have a name."
        periodInputLayout.error = "The delay must not be zero."
        durationInputLayout.error = "The duration must not be zero."
        randomInputLayout.error = "The randomize must be between 0 and 100."

        val nameObservable = name.textChanges().map { it.length != 0 }.distinctUntilChanged()
        val periodicityObservable = periodicity.textChanges().map { it.length != 0 }.distinctUntilChanged()
        val durationObservable = duration.textChanges().map { it.length != 0 }.distinctUntilChanged()
        val randomObservable = randomize.textChanges().map { it.length != 0 && IntRange(0, 100).contains(it.toString().toInt()) }.distinctUntilChanged().onErrorReturn { false }

        nameObservable.subscribe { nameInputLayout.isErrorEnabled = !it }
        periodicityObservable.subscribe { periodInputLayout.isErrorEnabled = !it }
        durationObservable.subscribe { durationInputLayout.isErrorEnabled = !it }
        randomObservable.subscribe { randomInputLayout.isErrorEnabled = !it }

        dialog.setPositiveButton("OK", this)
                .setNeutralButton("Cancel", this)

        val created = dialog.create()

        created.setOnShowListener {
            Observable.combineLatest(nameObservable, periodicityObservable, durationObservable, randomObservable, { nameValid, periodValid, durValid, randValid -> nameValid && periodValid && durValid && randValid })
                    .distinctUntilChanged()
                    .subscribe { created.getButton(Dialog.BUTTON_POSITIVE).isEnabled = it }
        }

        return created
    }

    override fun isResultValid(): Boolean {
        // todo: error messages!
        if (name.text.toString().isEmpty()) {
            return false
        } else if (duration.text.toString().isEmpty() || duration.text.toString().toInt() == 0) {
            return false
        } else if (periodicity.text.toString().isEmpty() || periodicity.text.toString().toInt() == 0) {
            return false
        }

        Snackbar.make(view, "Something is wrong.", Snackbar.LENGTH_LONG).show()
        return true
    }

    override fun onPositive() {
        if (schedule == null) {
            schedule = Schedule()
            schedule!!.generationEpoch = Instant.now().toEpochMilli()
        }

        schedule!!.name = name.text.toString()
        schedule!!.length = duration.text.toString().toInt()
        schedule!!.peroidicity = periodicity.text.toString().toInt()
        schedule!!.randomizePercent = (randomize.text.toString().toFloat() / 100)

        Realm.getDefaultInstance().use {
            it.executeTransaction { it.copyToRealmOrUpdate(schedule) }
        }
    }

    override fun onNeutral() {

    }

    override fun onNegative() {

    }
}