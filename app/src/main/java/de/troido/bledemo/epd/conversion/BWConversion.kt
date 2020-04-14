package de.troido.bledemo.epd.conversion

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import de.troido.bledemo.epd.bits.BitArray

class BWConversion {

    companion object {
        private val BLACK = SplitColor(Color.BLACK)
        private val WHITE = SplitColor(Color.WHITE)

        fun toBitmap(
                image: BitArray,
                @IntRange(from = 0) width: Int,
                @IntRange(from = 0) height: Int
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    bitmap.setPixel(x, y, if (image.get(y * width + x)) Color.BLACK else Color.WHITE)
                }
            }
            return bitmap
        }

        fun convertToBW(image: Bitmap): BitArray {
            val width = image.width
            val height = image.height

            val colors = ColorUtil.toSplitColors(image)

            val converted = BitArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color = colors[y * width + x]
                    val bw = toBW(color.toColor())
                    converted.set(y * width + x, !bw)

                    val err = color.sub(if (bw) WHITE else BLACK)

                    if (x + 1 < width) {
                        colors[y * width + x + 1].addErrMut(err, 7)
                        if (y + 1 < height) {
                            colors[(y + 1) * width + x + 1].addErrMut(err, 1)
                        }
                    }

                    if (y + 1 < height) {
                        colors[(y + 1) * width + x].addErrMut(err, 5)
                        if (x > 0) {
                            colors[(y + 1) * width + x - 1].addErrMut(err, 3)
                        }
                    }
                }
            }

            return converted
        }

        private fun toLin(@IntRange(from = 0x0, to = 0xff) srgb: Int): Double {
            val scaledSrgb = srgb.toDouble() / 0xff
            return if (scaledSrgb <= 0.04045)
                scaledSrgb / 12.92
            else
                Math.pow((scaledSrgb + 0.055) / 1.055, 2.4)
        }

        /**
         * Converts the given ARGB color to black (false) or white (true).
         */
        private fun toBW(@ColorInt color: Int): Boolean {
            val lin = (0.2126 * toLin(Color.red(color))
                    + 0.7152 * toLin(Color.green(color))
                    + 0.0722 * toLin(Color.red(color)))

            val srgb = if (lin <= 0.0031308)
                12.92 * lin
            else
                1.055 * Math.pow(lin, 1 / 2.4)

            return srgb >= 0.5
        }
    }

}