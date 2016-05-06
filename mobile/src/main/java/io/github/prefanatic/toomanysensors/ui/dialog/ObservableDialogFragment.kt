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

import android.app.DialogFragment
import android.content.DialogInterface
import rx.Observable
import rx.subjects.PublishSubject

/**
 * io.github.prefanatic.toomanysensors.ui.dialog (Cody Goldberg - 4/10/2016)
 */

abstract class ObservableDialogFragment<T> : DialogFragment(), DialogInterface.OnClickListener {
    private val subject: PublishSubject<T> = PublishSubject.create()
    val resultObservable: Observable<T>
        get() = subject.asObservable()

    abstract val result: T?

    open fun isResultValid() = true

    open fun onPositive() {

    }

    open fun onNeutral() {

    }

    open fun onNegative() {

    }

    override fun onDismiss(dialog: DialogInterface?) {
        subject.onCompleted()
        super.onDismiss(dialog)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                if (isResultValid()) {
                    onPositive()

                    if (result != null) {
                        subject.onNext(result)
                    }
                }
            }

            DialogInterface.BUTTON_NEUTRAL -> {
                onNeutral()
            }

            DialogInterface.BUTTON_NEGATIVE -> {

            }
        }

        if (which == DialogInterface.BUTTON_POSITIVE && isResultValid() || which != DialogInterface.BUTTON_POSITIVE)
            dismiss()
    }
}