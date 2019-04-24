package de.troido.bledemo.epd

interface CameraActivityListener {
    fun onBLEnMessageTransferFinished()
    fun onBLEnMessageTransferFailed()
    fun onBLEConnected()
}