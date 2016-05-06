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
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.wearable.Node
import com.jakewharton.rxbinding.view.clicks
import edu.uri.egr.hermeswear.HermesWearable
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.adapter.SensorDataAdapter
import io.github.prefanatic.toomanysensors.data.DataManager
import io.github.prefanatic.toomanysensors.data.dto.SensorData
import io.github.prefanatic.toomanysensors.data.dto.WearableSensor
import io.github.prefanatic.toomanysensors.extension.*
import io.github.prefanatic.toomanysensors.manager.SensorDataBus
import io.github.prefanatic.toomanysensors.ui.view.NodeSelectView
import io.github.prefanatic.toomanysensors.ui.view.SensorSelectView
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

class ObserveFragment : Fragment() {
    val mFab by bindView<FloatingActionButton>(R.id.fab)
    val mProgressBar by bindView<ProgressBar>(R.id.progress_bar)
    val mDataList by bindView<RecyclerView>(R.id.data_list)
    val mErrorText by bindView<TextView>(R.id.error_text)
    val mSensorList by bindView<SensorSelectView>(R.id.sensor_list)
    val nodeSelect by bindView<NodeSelectView>(R.id.node_select)

    val mSensorMap = HashMap<Int, String>()
    val mLifecycleSubscription = CompositeSubscription()

    var mIsActive = false
    var mDataAdapter: SensorDataAdapter? = null
    var mSensorDataSubscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_observe, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDataAdapter = SensorDataAdapter(activity)
        mDataList.adapter = mDataAdapter
        mDataList.itemAnimator = null
        mDataList.layoutManager = LinearLayoutManager(activity)

        if (savedInstanceState == null) {
            refreshNodeList()
        }

        if (mIsActive) {
            mDataList.visibility = View.VISIBLE
            mSensorList.visibility = View.GONE
        } else {
            mDataList.visibility = View.GONE
            mSensorList.visibility = View.VISIBLE
        }

        nodeSelect.getSelectionObservable()
                .subscribe { askWearableForSensorList(it) }

        mFab.clicks()
                .subscribe {
                    if (nodeSelect.getSize() == 0) {
                        refreshNodeList()
                    } else {
                        val node = nodeSelect.getSelected()
                        if (mIsActive) sendStopRequest(node) else sendStartRequest(node)
                    }
                }

        subscribeToDispatch()
    }

    override fun onDestroy() {
        mLifecycleSubscription.unsubscribe()
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mIsActive = savedInstanceState?.getBoolean(STATE_ACTIVE) ?: false
        /*mSelectedNode = savedInstanceState?.getInt(STATE_NODE_SELECTED) ?: -1

        // Populate the node map.
        val nodeNames = savedInstanceState?.getStringArray(STATE_NODE_NAMES)
        val nodeIds = savedInstanceState?.getStringArray(STATE_NODE_ID)
        if (nodeNames != null && nodeIds != null) {
            for (i in 0..nodeNames.size - 1) {
                mNodeMap.put(nodeNames[i], nodeIds[i])
                mNodeList.add(nodeNames[i])
            }

            mSpinnerAdapter?.notifyDataSetChanged()

            if (mSelectedNode != -1)
                mSpinner.setSelection(mSelectedNode)
        }*/

        // Populate the sensor map.
        val sensorNames = savedInstanceState?.getStringArray(STATE_SENSOR_NAMES)
        val sensorTypes = savedInstanceState?.getIntArray(STATE_SENSOR_TYPES)
        if (sensorNames != null && sensorTypes != null && sensorNames.size != 0 && sensorTypes.size != 0) {
            for (i in 0..sensorTypes.size - 1) {
                mSensorMap.put(sensorTypes[i], sensorNames[i])
                mSensorList.addSensor(WearableSensor(sensorNames[i], sensorTypes[i]))
            }
        }

        // Populate the selected sensors.
        val selectedSensors = savedInstanceState?.getIntArray(STATE_SENSOR_SELECTED)
        if (selectedSensors != null)
            mSensorList.setSelected(selectedSensors.toList())

        super.onActivityCreated(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putBoolean(STATE_ACTIVE, mIsActive)

            // Save the node map.
          /*  putStringArray(STATE_NODE_NAMES, mNodeMap.keys.toTypedArray())
            putStringArray(STATE_NODE_ID, mNodeMap.values.toTypedArray())*/

            // Save the sensor map.
            putIntArray(STATE_SENSOR_TYPES, mSensorMap.keys.toIntArray())
            putStringArray(STATE_SENSOR_NAMES, mSensorMap.values.toTypedArray())

            // Save the selected sensor.
            putIntArray(STATE_SENSOR_SELECTED, mSensorList.getSelected().toIntArray())

            // Save the selected node
            //putInt(STATE_NODE_SELECTED, mSelectedNode)
        }

        super.onSaveInstanceState(outState)
    }

    private fun refreshNodeList() {
        mProgressBar.simpleShow()

        HermesWearable.Node.nodes
                .doOnCompleted { nodeCompleted() }
                .subscribe { nodeReceived(it) }
    }

    private fun setError(error: String) {
        mErrorText.text = error
        mErrorText.simpleShow()
        mProgressBar.simpleHide()
    }

    private fun sendStartRequest(id: String) {
        val selectedSensors = mSensorList.getSelected()
        val wearableSensorList = ArrayList<WearableSensor>()

        selectedSensors.forEach {
            wearableSensorList.add(WearableSensor(mSensorMap[it]!!, it))
        }

        sendStartRequest(id, wearableSensorList)
        DataManager.get().setSensors(wearableSensorList)
    }

    private fun askWearableForSensorList(id: String) {
        mSensorMap.clear()
        mSensorList.clear()

        readSensorsFromWearable(id)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted {
                    mProgressBar.simpleHide()
                }
                .subscribe {
                    mSensorList.addSensor(it)
                    mSensorMap.put(it.type, it.name)
                }
    }

    private fun subscribeToDispatch() {
        with(mLifecycleSubscription) {
            add(HermesWearable.getPeerConnected()
                    .filter { it.isNearby }
                    .subscribe { nodeReceived(it) })

            add(HermesWearable.getPeerDisconnected()
                    .filter { it.isNearby }
                    .subscribe { nodeRemoved(it) })

            add(HermesWearable.getChannelOpened()
                    .filter { it.channel.path.equals(PATH_TRANSFER_DATA) }
                    .subscribe { handleDataStart() })

            add(HermesWearable.getInputClosed()
                    .filter { it.channel.path.equals(PATH_TRANSFER_DATA) }
                    .subscribe { handleDataEnd() })

            add(HermesWearable.getMessageEvent()
                    .filter { it.path.equals(PATH_ERROR) }
                    .subscribe { handleErrorReceived(String(it.data)) })
        }
    }

    private fun handleErrorReceived(msg: String) {
        Snackbar.make(mSensorList, "Error: $msg", Snackbar.LENGTH_INDEFINITE).show()
    }

    private fun updateUiForActive() {
        if (mIsActive) {
            mFab.setImageResource(R.drawable.ic_stop_24dp)
            mSensorList.simpleHide()
            mDataList.simpleShow()
        } else {
            mSensorList.simpleShow()
            mDataList.simpleHide()
            mFab.setImageResource(R.drawable.ic_play_arrow_24dp)
        }
    }

    private fun updateUiWithSensorData(sensorData: SensorData) {
        mDataAdapter?.updateSensorData(sensorData)
    }

    private fun updateDataListWithSelectedSensors() {
        val selectedList = mSensorList.getSelected()
        val produced = ArrayList<WearableSensor>()

        selectedList.forEach {
            produced.add(WearableSensor(mSensorMap[it]!!, it))
        }

        mDataAdapter?.setSensors(produced)
    }

    private fun handleDataEnd() {
        mIsActive = false
        mSensorDataSubscription?.unsubscribe()
        updateUiForActive()
    }

    private fun handleDataStart() {
        mIsActive = true
        mSensorDataSubscription = SensorDataBus.asObservable().observeOn(AndroidSchedulers.mainThread()).subscribe { updateUiWithSensorData(it) }

        updateDataListWithSelectedSensors()
        updateUiForActive()
    }

    private fun nodeCompleted() {
        if (nodeSelect.getSize() == 0) {
            nodeSelect.visibility = View.GONE
            mFab.setImageResource(R.drawable.ic_refresh_24dp)

            setError("No nodes detected.")
        }
    }

    private fun nodeReceived(node: Node) {
        if (nodeSelect.getSelectedRaw() == -1)
            mFab.setImageResource(R.drawable.ic_play_arrow_24dp)

        nodeSelect.addNode(node)
    }

    private fun nodeRemoved(node: Node) {
        nodeSelect.removeNode(node)
    }
}