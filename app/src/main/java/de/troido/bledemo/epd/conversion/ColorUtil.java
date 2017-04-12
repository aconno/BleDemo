package de.troido.bledemo.epd.conversion;

import android.graphics.Bitmap;
import android.support.annotation.IntRange;

public final class ColorUtil {
    private ColorUtil() {}

    @IntRange(from = 0x0, to = 0xff)
    public static int clampChannel(int x) {
        return Math.min(0xff, Math.max(0x00, x));
    }

    public static SplitColor[] toSplitColors(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        SplitColor[] colors = new SplitColor[width * height];
        for (int i = 0; i < pixels.length; i++) {
            colors[i] = new SplitColor(pixels[i]);
        }

        return colors;
    }
}
