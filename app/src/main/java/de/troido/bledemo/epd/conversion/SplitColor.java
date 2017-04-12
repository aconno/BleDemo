package de.troido.bledemo.epd.conversion;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * Color split into channels which can take up negative values for dithering
 * error purposes.
 */
public final class SplitColor {
    private int r;
    private int g;
    private int b;

    public SplitColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public SplitColor(@ColorInt int color) {
        this(Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Adds the given error color multiplied by the given numerator
     * (with 16 as denominator), mutating this object.
     */
    public SplitColor addErrMut(SplitColor color, int numerator) {
        r += (color.r * numerator) >> 4;
        g += (color.g * numerator) >> 4;
        b += (color.b * numerator) >> 4;
        return this;
    }

    public SplitColor sub(SplitColor color) {
        return new SplitColor(
                r - color.r,
                g - color.g,
                b - color.b
        );
    }

    public double diff(SplitColor color) {
        return Math.pow(r - color.r, 2)
                + Math.pow(g - color.g, 2)
                + Math.pow(b - color.b, 2);
    }

    @ColorInt
    public int toColor() {
        return Color.argb(
                0xff,
                ColorUtil.clampChannel(r),
                ColorUtil.clampChannel(g),
                ColorUtil.clampChannel(b)
        );
    }
}
