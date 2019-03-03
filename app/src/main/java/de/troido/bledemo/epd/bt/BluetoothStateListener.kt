package de.troido.bledemo.epd.bt

interface BluetoothStateListener {
    fun onBluetoothTurnedOn()

    fun onBluetoothTurnedOff()
}