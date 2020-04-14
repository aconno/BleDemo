package de.troido.bledemo.epd.conversion

import android.graphics.Bitmap
import androidx.annotation.IntRange

object ColorUtil {
    @IntRange(from = 0x0, to = 0xFF)
    fun clampChannel(x: Int): Int {
        return x.coerceIn(0x00, 0xFF)
    }

    fun toSplitColors(image: Bitmap): Array<SplitColor> {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)
        return pixels.map { SplitColor(it) }.toTypedArray()
    }
}