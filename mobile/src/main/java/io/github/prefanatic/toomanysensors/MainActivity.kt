package io.github.prefanatic.toomanysensors

import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.ArrayAdapter
import android.widget.Spinner
import butterknife.bindView
import com.google.android.gms.wearable.Node
import edu.uri.egr.hermeswear.HermesWearable
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.test.todo

class MainActivity : AppCompatActivity() {
    val mToolbar by bindView<Toolbar>(R.id.toolbar)
    val mFab by bindView<FloatingActionButton>(R.id.fab)
    val mSpinner by bindView<Spinner>(R.id.node_spinner)
    val mSensorList by bindView<RecyclerView>(R.id.sensor_list)

    val mNodeList = ArrayList<String>()
    val mNodeMap = HashMap<String, String>()
    val mSensorMap = HashMap<Int, String>()
    val mSelectedSensors = ArrayList<Int>()
    val mLifecycleSubscription = CompositeSubscription()

    var mIsActive = false
    var mSpinnerAdapter: ArrayAdapter<String>? = null
    var mSensorAdapter: SensorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mToolbar)

        mSpinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mNodeList)
        mSpinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = mSpinnerAdapter

        mSensorAdapter = SensorAdapter()
        mSensorAdapter?.setSelected(mSelectedSensors)
        mSensorList.adapter = mSensorAdapter
        mSensorList.layoutManager = LinearLayoutManager(this)

        subscribeToDispatch()
    }

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
        val inputStream = DataInputStream(stream)

        try {
            for (i in 0..inputStream.readInt())
                mSensorMap.put(inputStream.readInt(), inputStream.readUTF())
        } catch (e: IOException) {
            Timber.e(e, "Failed to read sensor list.")
        }

        runOnUiThread {
            mSensorMap.forEach { mSensorAdapter?.addSensor(WearableSensor(it.value, it.key)) }
        }
    }

    private fun askWearableForSensorList(id: String) {
        mSensorMap.clear()
        mSensorAdapter?.clear()

        todo { "We need to unsubscribe this subscription!" }
        val subscription = HermesWearable.getChannelOpened()
                .filter { it.channel.path.equals(PATH_TRANSFER_SENSOR_LIST) }
                .flatMap { HermesWearable.Channel.getInputStream(it.channel) }
                .subscribe { readSensorList(it) }

        HermesWearable.Message.sendMessage(id, PATH_REQUEST_SENSORS)
                .subscribe { }
    }

    private fun subscribeToDispatch() {
        mLifecycleSubscription.add(HermesWearable.getPeerConnected()
                .filter { it.isNearby }
                .subscribe { nodeReceived(it) })
        mLifecycleSubscription.add(HermesWearable.getPeerDisconnected()
                .filter { it.isNearby }
                .subscribe { nodeRemoved(it) })
        mLifecycleSubscription.add(HermesWearable.getChannelOpened()
                .filter { it.channel.path.equals(PATH_TRANSFER_DATA) }
                .subscribe { handleDataStart() })
        mLifecycleSubscription.add(HermesWearable.getInputClosed()
                .filter { it.channel.path.equals(PATH_TRANSFER_DATA) }
                .subscribe { handleDataEnd() })
    }

    private fun updateUiForActive() {
        if (mIsActive)
            mFab.setImageResource(R.drawable.ic_stop_24dp)
        else
            mFab.setImageResource(R.drawable.ic_play_arrow_24dp)
    }

    private fun handleDataEnd() {
        mIsActive = false
        updateUiForActive()
    }

    private fun handleDataStart() {
        mIsActive = true
        updateUiForActive()
    }

    private fun nodeReceived(node: Node) {
        mNodeMap.put(node.displayName, node.id)
        mNodeList.add(node.displayName)

        mSpinnerAdapter?.notifyDataSetChanged()
    }

    private fun nodeRemoved(node: Node) {
        mNodeMap.remove(node.displayName)
        mNodeList.remove(node.displayName)

        mSpinnerAdapter?.notifyDataSetChanged()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        mIsActive = savedInstanceState?.getBoolean(STATE_ACTIVE) ?: false

        // Populate the node map.
        val nodeNames = savedInstanceState?.getStringArray(STATE_NODE_NAMES)
        val nodeIds = savedInstanceState?.getStringArrayList(STATE_NODE_ID)
        if (nodeNames != null && nodeIds != null) {
            for (i in 0..nodeNames.size) {
                mNodeMap.put(nodeNames[i], nodeIds[i])
                mNodeList.add(nodeNames[i])
            }

            mSpinnerAdapter?.notifyDataSetChanged()
        }

        // Populate the sensor map.
        val sensorNames = savedInstanceState?.getStringArray(STATE_SENSOR_NAMES)
        val sensorTypes = savedInstanceState?.getIntArray(STATE_SENSOR_TYPES)
        if (sensorNames != null && sensorTypes != null) {
            for (i in 0..sensorTypes.size) {
                mSensorMap.put(sensorTypes[i], sensorNames[i])
                mSensorAdapter?.addSensor(WearableSensor(sensorNames[i], sensorTypes[i]))
            }
        }

        // Populate the selected sensors.
        val selectedSensors = savedInstanceState?.getIntegerArrayList(STATE_SENSOR_SELECTED)
        if (selectedSensors != null)
            mSensorAdapter?.setSelected(selectedSensors)

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(STATE_ACTIVE, mIsActive)

        // Save the node map.
        outState?.putStringArray(STATE_NODE_NAMES, mNodeMap.keys.toTypedArray())
        outState?.putStringArray(STATE_NODE_ID, mNodeMap.values.toTypedArray())

        // Save the sensor map.
        outState?.putIntArray(STATE_SENSOR_TYPES, mSensorMap.keys.toIntArray())
        outState?.putStringArray(STATE_SENSOR_NAMES, mSensorMap.values.toTypedArray())

        // Save the selected sensor.
        outState?.putIntegerArrayList(STATE_SENSOR_SELECTED, mSensorAdapter?.getSelected())

        super.onSaveInstanceState(outState)
    }
}