package de.troido.bledemo.epd.bt

interface BluetoothImplListener {

    fun onMessageWriteFailed()

    fun onConnectionLost()

    fun onConnected()

    fun onAdapterOff()

}