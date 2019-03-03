package de.troido.bledemo.epd.bt

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class ScanCallback(private val scanResultFilteredDevice: ScanResultFilteredDevice): ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        Log.d("Bluetooth_scan", "Found device with name EMP")
        result?.device?.let {
            scanResultFilteredDevice.onDeviceFound(result.device)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e("Bluetooth_scan", "SCAN FAILED")
    }
}