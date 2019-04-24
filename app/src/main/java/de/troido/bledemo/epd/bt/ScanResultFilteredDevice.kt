package de.troido.bledemo.epd.bt

import android.bluetooth.BluetoothDevice

interface ScanResultFilteredDevice {
    fun onDeviceFound(device: BluetoothDevice)
}