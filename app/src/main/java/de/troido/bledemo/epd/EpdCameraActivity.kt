package de.troido.bledemo.epd

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import de.troido.bledemo.Application
import de.troido.bledemo.R
import de.troido.bledemo.epd.bits.BitArray
import de.troido.bledemo.epd.bt.BluetoothImpl
import de.troido.bledemo.epd.bt.BluetoothImplListener
import de.troido.bledemo.epd.bt.BluetoothStateListener
import de.troido.bledemo.epd.bt.BluetoothStateReceiver
import kotlinx.android.synthetic.main.activity_epd_camera.*

class EpdCameraActivity: AppCompatActivity(), CameraResultListener, BluetoothImplListener, BluetoothStateListener {
    private val bluetoothImpl = BluetoothImpl(this, this)


    val bluetoothStateReceiver = BluetoothStateReceiver(this)

    override fun onMessageWriteFailed() {
        Snackbar.make(ept_activity_layout,"Not Connected",Snackbar.LENGTH_LONG).show()
    }

    override fun onConnectionLost() {
        Snackbar.make(ept_activity_layout,"Connection lost",Snackbar.LENGTH_INDEFINITE).setAction("Rescan") {
            progressbar?.visibility = View.VISIBLE
            bluetoothImpl.startScan()
        }.show()
    }

    override fun onConnected() {
        this.runOnUiThread {
            progressbar?.postOnAnimation {
                progressbar?.visibility = View.GONE
                Handler().postDelayed({
                    Toast.makeText(this,"Connected", Toast.LENGTH_SHORT).show()
                },500)
            }
        }
    }

    override fun onAdapterOff() {
        this.runOnUiThread {
            progressbar?.visibility = View.GONE
            Snackbar.make(ept_activity_layout, "Bluetooth Off", Snackbar.LENGTH_INDEFINITE).setAction("Turn On"){
                bluetoothImpl.turnOnBluetooth()
            }.show()
        }
    }

    override fun onBluetoothTurnedOn() {
        this.runOnUiThread {
            Snackbar.make(ept_activity_layout, "Bluetooth Turned On", Snackbar.LENGTH_INDEFINITE).setAction("Rescan"){
                bluetoothImpl.startScan()
            }.show()
        }
    }

    override fun onBluetoothTurnedOff() {
        onAdapterOff()
    }

    override fun onCameraResult(bitArray: BitArray) {
        bluetoothImpl.writeLongMessage(bitArray.toByteArray())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epd_camera)
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))


        val cameraFragment = EpdCameraFragment()
        cameraFragment.cameraResultListener = this

        supportFragmentManager.beginTransaction()
                .replace(R.id.camera_frame_layout, cameraFragment)
                .commit()

        bluetoothImpl.startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothImpl.stopScan()
        bluetoothImpl.closeGatt()
        unregisterReceiver(bluetoothStateReceiver)
    }

}