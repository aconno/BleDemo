package de.troido.bledemo.epd

import de.troido.bledemo.epd.bits.BitArray

interface CameraResultListener {
    fun onCameraResult(bitArray: BitArray)
}