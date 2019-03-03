package de.troido.bledemo.epd.bt

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BluetoothStateReceiver(private val bluetoothStateListener: BluetoothStateListener?) : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    bluetoothStateListener?.onBluetoothTurnedOff()
                }

                BluetoothAdapter.STATE_TURNING_ON -> {
                }

                BluetoothAdapter.STATE_ON -> {
                    bluetoothStateListener?.onBluetoothTurnedOn()
                }

                BluetoothAdapter.STATE_TURNING_OFF -> {
                }
            }
        }
    }
}