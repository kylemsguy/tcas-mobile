package com.kylemsguy.tcasmobile.backend;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TCaSImageConverter {
    private static final int[] BACKGROUND_COLOUR = {255, 255, 255}; // This is the proper spelling. Trust me. I'm Canadian.
    public static final int[] IMAGE_DIMENSIONS = {32, 32};

    private int[] argbAry = new int[1024];

    /**
     * Loads the image into a new TCaSImageConverter object
     *
     * @param image The desired 32x32 image as a Bitmap
     * @throws InvalidFileException
     */
    public TCaSImageConverter(Bitmap image) throws InvalidFileException {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width != IMAGE_DIMENSIONS[0] || height != IMAGE_DIMENSIONS[1]) {
            throw new InvalidFileException("Unsupported image size (" + width + "x" + height + ")");
        }

        image.getPixels(argbAry, 0, width, 0, 0, width, height);
    }

    /**
     * Does the conversion and returns the data as a String.
     *
     * @return converted image data
     */
    public String convertToTCaSImg() {
        return convertToTCaSImg(BACKGROUND_COLOUR);
    }

    /**
     * Does the conversion with bgcolour as the background colour,
     * and returns the data as a String.
     *
     * @param bgcolour the background colour for images with transparency
     * @return converted image data
     */
    public String convertToTCaSImg(int[] bgcolour) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < argbAry.length - 1; i++) {
            int[] rgb = argb2Rgb(argbAry[i], bgcolour);

            sb.append(scale(rgb[0], 255, 63));
            sb.append("|");
            sb.append(scale(rgb[1], 255, 63));
            sb.append("|");
            sb.append(scale(rgb[2], 255, 63));
            sb.append(",");

            System.out.println("RED: " + rgb[0] + " GREEN: " + rgb[1] + " BLUE: " + rgb[2]);

        }

        int[] lastPixel = argb2Rgb(argbAry[argbAry.length - 1], bgcolour);

        sb.append(scale(lastPixel[0], 255, 63));
        sb.append("|");
        sb.append(scale(lastPixel[1], 255, 63));
        sb.append("|");
        sb.append(scale(lastPixel[2], 255, 63));

        System.out.println("RED: " + lastPixel[0] + " GREEN: " + lastPixel[1] + "BLUE: " + lastPixel[2]);

        System.out.println("Final string: " + sb.toString());

        return sb.toString();
    }

    /**
     * Applies a background colour to an int packed ARGB value
     *
     * @param argb       Integer packed ARGB value
     * @param background RGB array for the background colour (length 3)
     * @return An array of RGB values (length 3)
     */
    public static int[] argb2Rgb(int argb, int[] background) {
        int alpha = argb >> 24 & 255;

        int red = argb >> 16 & 255;
        int green = argb >> 8 & 255;
        int blue = argb & 255;

        int newRed = pixelargb2Rgb(alpha, red, background[0]);
        int newGreen = pixelargb2Rgb(alpha, green, background[1]);
        int newBlue = pixelargb2Rgb(alpha, blue, background[2]);
        return new int[]{newRed, newGreen, newBlue};
    }

    /**
     * Extracts image data from www.twocansandstring.com/profile/draw
     *
     * @param html raw HTML from the page
     * @return Profile image in TCaS Image format
     */
    public static String extractImgData(String html) throws IllegalArgumentException {
        Document dom = Jsoup.parse(html);
        // this is a workaround for a bug
        Elements divtags = dom.getElementsByTag("div");
        Element imgdatatag = null;
        for (Element e : divtags) {
            if (e.hasAttr("id") && e.attr("id").equals("draw_current_avatar"))
                imgdatatag = e;
        }
        if (imgdatatag != null) {
            return imgdatatag.text();
        } else
            throw new IllegalArgumentException("Invalid HTML");
    }

    /**
     * Creates a Bitmap from a TCaS Image string
     *
     * @param imgText Image in TCaS Image string format
     * @return a bitmap created from the string
     * @throws IllegalArgumentException
     */
    public static Bitmap textToBitmap(String imgText) throws IllegalArgumentException {
        System.out.println("textToBitmap: Incoming data: " + imgText);
        int numPixels = IMAGE_DIMENSIONS[0] * IMAGE_DIMENSIONS[1];
        String[] strPixels = imgText.split(",");
        System.err.println("textToBitmap: " + strPixels.length + " " + numPixels);
        if (strPixels.length != numPixels) {
            throw new IllegalArgumentException("Incorrect image size (" + strPixels.length + ")");
        }
        int[] pixels = new int[numPixels];
        for (int i = 0; i < numPixels; i++) {
            String[] rgbPixel = strPixels[i].split("\\|");
            try {
                int red = scale(Integer.parseInt(rgbPixel[0]), 63, 255);
                int green = scale(Integer.parseInt(rgbPixel[1]), 63, 255);
                int blue = scale(Integer.parseInt(rgbPixel[2]), 63, 255);
                pixels[i] = Color.rgb(red, green, blue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid image data");
            }
        }
        return Bitmap.createBitmap(pixels, IMAGE_DIMENSIONS[0], IMAGE_DIMENSIONS[1], Bitmap.Config.ARGB_8888);
    }

    public static int scale(int value, int currMax, int newMax) {
        // ratio assumes range is [0, max+1)
        double ratio = (newMax + 1) / (currMax + 1);
        return (int) (value * ratio);
    }

    private static int pixelargb2Rgb(int alpha, int original, int background) {
        int opacity = alpha / 255;
        return opacity * original + (1 - opacity) * background;
    }

    public String toString() {
        // TODO implement
        return super.toString();
    }

}
