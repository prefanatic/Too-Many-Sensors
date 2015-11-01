package io.github.prefanatic.toomanysensors.ui.fragment

import android.app.Fragment
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.wearable.Node
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.itemSelections
import edu.uri.egr.hermeswear.HermesWearable
import io.github.prefanatic.toomanysensors.R
import io.github.prefanatic.toomanysensors.adapter.SensorAdapter
import io.github.prefanatic.toomanysensors.adapter.SensorDataAdapter
import io.github.prefanatic.toomanysensors.data.SensorData
import io.github.prefanatic.toomanysensors.data.WearableSensor
import io.github.prefanatic.toomanysensors.extension.*
import io.github.prefanatic.toomanysensors.manager.SensorDataBus
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

class ObserveFragment : Fragment() {
    val mToolbar by bindView<Toolbar>(R.id.toolbar)
    val mFab by bindView<FloatingActionButton>(R.id.fab)
    val mProgressBar by bindView<ProgressBar>(R.id.progress_bar)
    val mSpinner by bindView<Spinner>(R.id.node_spinner)
    val mSensorList by bindView<RecyclerView>(R.id.sensor_list)
    val mDataList by bindView<RecyclerView>(R.id.data_list)
    val mErrorText by bindView<TextView>(R.id.error_text);

    val mNodeList = ArrayList<String>()
    val mNodeMap = HashMap<String, String>()
    val mSensorMap = HashMap<Int, String>()
    val mSelectedSensors = ArrayList<Int>()
    val mLifecycleSubscription = CompositeSubscription()

    var mSelectedNode = -1
    var mIsActive = false
    var mSpinnerAdapter: ArrayAdapter<String>? = null
    var mSensorAdapter: SensorAdapter? = null
    var mDataAdapter: SensorDataAdapter? = null
    var mSensorListSubscription: Subscription? = null
    var mSensorDataSubscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_observe, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSpinnerAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, mNodeList)
        mSpinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = mSpinnerAdapter

        mSensorAdapter = SensorAdapter()
        mSensorAdapter?.setSelected(mSelectedSensors)
        mSensorList.adapter = mSensorAdapter
        mSensorList.layoutManager = LinearLayoutManager(activity)

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

        mSpinner.itemSelections()
                .filter { it != -1 && it != mSelectedNode }
                .subscribe {
                    mSelectedNode = it
                    askWearableForSensorList(getNodeIdFromAdapter(it)!!)
                }

        mFab.clicks()
                .subscribe {
                    if (mNodeList.size == 0) {
                        refreshNodeList()
                    } else {
                        val node = getNodeIdFromAdapter(mSelectedNode)!!
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
        mSelectedNode = savedInstanceState?.getInt(STATE_NODE_SELECTED) ?: -1

        // Populate the node map.
        val nodeNames = savedInstanceState?.getStringArray(STATE_NODE_NAMES)
        val nodeIds = savedInstanceState?.getStringArrayList(STATE_NODE_ID)
        if (nodeNames != null && nodeIds != null) {
            for (i in 0..nodeNames.size) {
                mNodeMap.put(nodeNames[i], nodeIds[i])
                mNodeList.add(nodeNames[i])
            }

            mSpinnerAdapter?.notifyDataSetChanged()

            if (mSelectedNode != -1)
                mSpinner.setSelection(mSelectedNode)
        }

        // Populate the sensor map.
        val sensorNames = savedInstanceState?.getStringArray(STATE_SENSOR_NAMES)
        val sensorTypes = savedInstanceState?.getIntArray(STATE_SENSOR_TYPES)
        if (sensorNames != null && sensorTypes != null && sensorNames.size != 0 && sensorTypes.size != 0) {
            for (i in 0..sensorTypes.size) {
                mSensorMap.put(sensorTypes[i], sensorNames[i])
                mSensorAdapter?.addSensor(WearableSensor(sensorNames[i], sensorTypes[i]))
            }
        }

        // Populate the selected sensors.
        val selectedSensors = savedInstanceState?.getIntegerArrayList(STATE_SENSOR_SELECTED)
        if (selectedSensors != null)
            mSensorAdapter?.setSelected(selectedSensors)

        super.onActivityCreated(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putBoolean(STATE_ACTIVE, mIsActive)

            // Save the node map.
            putStringArray(STATE_NODE_NAMES, mNodeMap.keys.toTypedArray())
            putStringArray(STATE_NODE_ID, mNodeMap.values.toTypedArray())

            // Save the sensor map.
            putIntArray(STATE_SENSOR_TYPES, mSensorMap.keys.toIntArray())
            putStringArray(STATE_SENSOR_NAMES, mSensorMap.values.toTypedArray())

            // Save the selected sensor.
            putIntegerArrayList(STATE_SENSOR_SELECTED, mSensorAdapter?.getSelected())

            // Save the selected node
            putInt(STATE_NODE_SELECTED, mSelectedNode)
        }

        super.onSaveInstanceState(outState)
    }

    private fun refreshNodeList() {
        HermesWearable.Node.nodes
                .doOnCompleted { nodeCompleted() }.
                subscribe { nodeReceived(it) }
    }

    private fun setError(error: String) {
        mErrorText.text = error
        mErrorText.simpleShow()
        mProgressBar.simpleHide()
    }

    private fun getNodeIdFromAdapter(i: Int) = mNodeMap[mNodeList[i]]

    private fun sendStartRequest(id: String) {
        val selectedSensors = mSensorAdapter?.getSelected() ?: return
        val buffer = ByteBuffer.allocate(12 + (4 * selectedSensors.size))

        buffer.putInt(SensorManager.SENSOR_DELAY_NORMAL)
        buffer.putInt(0)
        buffer.putInt(selectedSensors.size)

        selectedSensors.forEach { buffer.putInt(it) }

        HermesWearable.Message.sendMessage(id, PATH_START, buffer.array())
                .subscribe { }
    }

    private fun sendStopRequest(id: String) {
        HermesWearable.Message.sendMessage(id, PATH_STOP)
                .subscribe { }
    }

    private fun readSensorList(stream: InputStream) {
        DataInputStream(stream).use {
            for (i in 1..it.readInt())
                mSensorMap.put(it.readInt(), it.readUTF())
        }

        runOnUiThread {
            mSensorMap.forEach { mSensorAdapter?.addSensor(WearableSensor(it.value, it.key)) }
            mProgressBar.simpleHide()
        }

        mSensorListSubscription?.unsubscribe()
    }

    private fun askWearableForSensorList(id: String) {
        mSensorMap.clear()
        mSensorAdapter?.clear()

        mSensorListSubscription = HermesWearable.getChannelOpened()
                .filter { it.channel.path.equals(PATH_TRANSFER_SENSOR_LIST) }
                .flatMap { HermesWearable.Channel.getInputStream(it.channel) }
                .subscribe { readSensorList(it) }

        HermesWearable.Message.sendMessage(id, PATH_REQUEST_SENSORS)
                .subscribe { }
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
        }
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
        val selectedList = mSensorAdapter?.getSelected()
        val produced = ArrayList<WearableSensor>()

        selectedList?.forEach {
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
        if (mNodeList.size == 0) {
            mSpinner.visibility = View.GONE
            mFab.setImageResource(R.drawable.ic_refresh_24dp)

            setError("No nodes detected.")
        }
    }

    private fun nodeReceived(node: Node) {
        if (mSelectedNode == -1)
            mFab.setImageResource(R.drawable.ic_play_arrow_24dp)

        mNodeMap.put(node.displayName, node.id)
        mNodeList.add(node.displayName)

        mSpinnerAdapter?.notifyDataSetChanged()
    }

    private fun nodeRemoved(node: Node) {
        mNodeMap.remove(node.displayName)
        mNodeList.remove(node.displayName)

        mSpinnerAdapter?.notifyDataSetChanged()
    }
}