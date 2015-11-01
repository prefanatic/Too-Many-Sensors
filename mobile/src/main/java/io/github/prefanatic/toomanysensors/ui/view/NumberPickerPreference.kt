package io.github.prefanatic.toomanysensors.ui.view

import android.content.Context
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import com.jakewharton.rxbinding.widget.itemSelections
import com.jakewharton.rxbinding.widget.textChanges
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.extension.PREF_SAMPLING_RATE
import io.github.prefanatic.toomanysensors.extension.PREF_SAMPLING_RATE_UNIT
import io.github.prefanatic.toomanysensors.extension.pow

class NumberPickerPreference : DialogPreference {
    var number = 0
    var unit = 0

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?) : this(context, null)

    init {
        dialogLayoutResource = R.layout.preference_number_picker
        positiveButtonText = "OK"
        negativeButtonText = "Cancel"
    }

    private fun getStandardNumber(): Int {
        when (unit) {
            0 -> return (1 / number) * 10.pow(9)
            1 -> return number
            2 -> return number * 10.pow(-3)
            3 -> return number * 10.pow(-6)
            4 -> return number * 10.pow(-9)
            else -> throw Exception("Unit conversion $unit is not implemented.")
        }
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        view?.run {
            val textValue = findViewById(R.id.picker_text) as EditText
            val unitSpinner = findViewById(R.id.picker_spinner) as Spinner

            number = sharedPreferences.getInt(PREF_SAMPLING_RATE, 0)
            unit = sharedPreferences.getInt(PREF_SAMPLING_RATE_UNIT, 0)

            unitSpinner.setSelection(unit)
            unitSpinner.itemSelections()
                    .subscribe { unit = it }

            textValue.setText(number.toString())
            textValue.textChanges()
                    .map { it.toString() }
                    .subscribe {
                        number = if (it.isNotEmpty()) it.toInt() else 0
                    }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            sharedPreferences.edit()
                    .putInt(PREF_SAMPLING_RATE, getStandardNumber())
                    .putInt(PREF_SAMPLING_RATE_UNIT, unit)
                    .apply()
        }
    }
}