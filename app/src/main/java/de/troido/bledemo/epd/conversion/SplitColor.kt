package de.troido.bledemo.epd.conversion

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.math.pow

/**
 * Color split into channels which can take up negative values for dithering
 * error purposes.
 */
class SplitColor(private var r: Int, private var g: Int, private var b: Int) {

    constructor(@ColorInt color: Int) : this(Color.red(color), Color.green(color), Color.blue(color)) {}

    /**
     * Adds the given error color multiplied by the given numerator
     * (with 16 as denominator), mutating this object.
     */
    fun addErrMut(color: SplitColor, numerator: Int): SplitColor {
        r += color.r * numerator shr 4
        g += color.g * numerator shr 4
        b += color.b * numerator shr 4
        return this
    }

    fun sub(color: SplitColor): SplitColor {
        return SplitColor(
            r - color.r,
            g - color.g,
            b - color.b
        )
    }

    fun diff(color: SplitColor): Double {
        return ((r - color.r.toDouble()).pow(2.0)
            + (g - color.g.toDouble()).pow(2.0)
            + (b - color.b.toDouble()).pow(2.0))
    }

    @ColorInt
    fun toColor(): Int {
        return Color.argb(
            0xFF,
            ColorUtil.clampChannel(r),
            ColorUtil.clampChannel(g),
            ColorUtil.clampChannel(b)
        )
    }

}