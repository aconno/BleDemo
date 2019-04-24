package de.troido.bledemo.epd.bt

import android.bluetooth.BluetoothGattCharacteristic

interface BleCharacteristicListener {
    fun onTxCharacteristicFound(gattCharacteristic: BluetoothGattCharacteristic)
}