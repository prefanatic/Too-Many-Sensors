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

package io.github.prefanatic.toomanysensors.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import com.google.android.gms.wearable.Node
import com.jakewharton.rxbinding.widget.itemSelections
import rx.Observable
import java.util.*

/**
 * io.github.prefanatic.toomanysensors.ui.view (Cody Goldberg - 3/30/2016)
 */
class NodeSelectView : FrameLayout {
    private val spinner: Spinner
    private val adapter: ArrayAdapter<String>
    private val nodeList = ArrayList<String>()
    private val nodeMap = HashMap<String, String>()

    private var selectedNode = -1

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        spinner = Spinner(context)
        adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, nodeList)

        spinner.adapter = adapter

        addView(spinner)
    }

    fun addNode(node: Node) {
        nodeMap.put(node.displayName, node.id)
        nodeList.add(node.displayName)

        adapter.notifyDataSetChanged()
    }

    fun removeNode(node: Node) {
        nodeMap.remove(node.displayName)
        nodeList.remove(node.displayName)

        adapter.notifyDataSetChanged()
    }

    fun getSize() = nodeList.size

    fun getSelectionObservable(): Observable<String> =
            spinner.itemSelections()
                    .filter { it != -1 && it != selectedNode }
                    .doOnNext { selectedNode = it }
                    .map { nodeMap[nodeList[it]] }


    fun getSelected(): String =
            nodeMap[nodeList[selectedNode]]!!

    fun getSelectedRaw() =
            selectedNode

}