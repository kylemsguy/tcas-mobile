package com.kylemsguy.tcasmobile.backend;

import android.graphics.Bitmap;

public class TCaSImageConverter {
    private static final int[] BACKGROUND_COLOUR = {255, 255, 255}; // This is the proper spelling. Trust me. I'm Canadian.

    private int[] argbAry;

    public TCaSImageConverter(Bitmap image) throws InvalidFileException {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width != 32 || height != 32) {
            throw new InvalidFileException("Unsupported image size (" + width + "x" + height + ")");
        }

        image.getPixels(argbAry, 0, width, 0, 0, width, height);
    }

    public String convertToTCaSImg() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < argbAry.length - 1; i++) {
            int[] rgb = ARGB2RGB(argbAry[i], BACKGROUND_COLOUR);

            sb.append(rgb[0] / 4);
            sb.append("|");
            sb.append(rgb[1] / 4);
            sb.append("|");
            sb.append(rgb[2] / 4);
            sb.append(",");

        }

        int[] lastPixel = ARGB2RGB(argbAry[argbAry.length - 1], BACKGROUND_COLOUR);

        sb.append(lastPixel[0] / 4);
        sb.append("|");
        sb.append(lastPixel[1] / 4);
        sb.append("|");
        sb.append(lastPixel[2] / 4);

        return sb.toString();
    }

    /**
     * Applies a background colour to an int packed ARGB value
     *
     * @param argb       Integer packed ARGB value
     * @param background RGB array for the background colour (length 3)
     * @return An array of RGB values (length 3)
     */
    public static int[] ARGB2RGB(int argb, int[] background) {
        int alpha = argb >> 24 & 255;

        int red = argb >> 16 & 255;
        int green = argb >> 8 & 255;
        int blue = argb & 255;

        int newRed = pixelARGB2RGB(alpha, red, background[0]);
        int newGreen = pixelARGB2RGB(alpha, green, background[1]);
        int newBlue = pixelARGB2RGB(alpha, blue, background[2]);
        return new int[]{newRed, newGreen, newBlue};
    }

    private static int pixelARGB2RGB(int alpha, int original, int background) {
        int opacity = alpha / 255;
        return opacity * original + (1 - opacity) * background;
    }

    public String toString() {
        // TODO implement
        return super.toString();
    }

}
