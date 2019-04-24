package de.troido.bledemo.epd.bt

interface BleConnStatusChangeListener {
    fun onBleConnectionStatusChange()

    fun onBleDisconnected()

    fun onBleConnected()
}