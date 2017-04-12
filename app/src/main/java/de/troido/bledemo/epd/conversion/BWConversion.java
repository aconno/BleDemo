package de.troido.bledemo.epd.conversion;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import de.troido.bledemo.epd.bits.BitArray;

public final class BWConversion {

    private static final SplitColor BLACK = new SplitColor(Color.BLACK);
    private static final SplitColor WHITE = new SplitColor(Color.WHITE);

    public static Bitmap toBitmap(
            BitArray image,
            @IntRange(from = 0) int width,
            @IntRange(from = 0) int height
    ) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bitmap.setPixel(x, y, image.get(y * width + x) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    public static BitArray convertToBW(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        SplitColor[] colors = ColorUtil.toSplitColors(image);

        BitArray converted = new BitArray(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                SplitColor color = colors[y * width + x];
                boolean bw = toBW(color.toColor());
                converted.set(y * width + x, !bw);

                SplitColor err = color.sub(bw ? WHITE : BLACK);

                if (x + 1 < width) {
                    colors[y * width + x + 1].addErrMut(err, 7);
                    if (y + 1 < height) {
                        colors[(y + 1) * width + x + 1].addErrMut(err, 1);
                    }
                }

                if (y + 1 < height) {
                    colors[(y + 1) * width + x].addErrMut(err, 5);
                    if (x > 0) {
                        colors[(y + 1) * width + x - 1].addErrMut(err, 3);
                    }
                }
            }
        }

        return converted;
    }

    private static double toLin(@IntRange(from = 0x0, to = 0xff) int srgb) {
        double scaledSrgb = (double) srgb / 0xff;
        return scaledSrgb <= 0.04045
                ? scaledSrgb / 12.92
                : Math.pow((scaledSrgb + 0.055) / 1.055, 2.4);
    }

    /**
     * Converts the given ARGB color to black (false) or white (true).
     */
    private static boolean toBW(@ColorInt int color) {
        double lin = 0.2126 * toLin(Color.red(color))
                + 0.7152 * toLin(Color.green(color))
                + 0.0722 * toLin(Color.red(color));

        double srgb = lin <= 0.0031308
                ? 12.92 * lin
                : 1.055 * Math.pow(lin, 1 / 2.4);

        return srgb >= 0.5;
    }
}
