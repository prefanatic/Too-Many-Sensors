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
import android.view.LayoutInflater
import android.widget.EditText
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.data.realm.Category
import io.realm.Realm

class CategoryEditDialog : ObservableDialogFragment<Category>() {
    lateinit var categoryEditText: EditText
    private var category: Category? = null

    override val result: Category?
        get() = category

    companion object {
        public fun newInstance(categoryName: String): CategoryEditDialog {
            val dialog = CategoryEditDialog()
            val bundle = Bundle()

            bundle.putString("category", categoryName)
            dialog.arguments = bundle

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val dialog = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_category_edit, null)

        categoryEditText = view.findViewById(R.id.edit_category) as EditText

        Realm.getInstance(activity).use {
            val cat = it.where(Category::class.java)
                    .equalTo("name", arguments.getString("category"))
                    .findFirst()

            if (cat != null)
                category = it.copyFromRealm(cat)
        }

        if (category != null) {
            categoryEditText.setText(category?.name)
        } else {
            category = Category()
        }

        dialog.setView(view)
                .setPositiveButton(R.string.category_positive, this)
                .setNeutralButton(R.string.category_neutral, this)
                .setTitle(R.string.category_title)

        return dialog.create()
    }

    override fun onPositive() {
        category?.name = categoryEditText.text.toString()
        Realm.getInstance(activity).use {
            it.executeTransaction {
                it.copyToRealmOrUpdate(category)
            }
        }
    }

    override fun onNeutral() {
    }

    override fun onNegative() {
    }
}