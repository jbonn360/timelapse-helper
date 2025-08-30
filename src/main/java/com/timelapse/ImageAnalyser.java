package com.timelapse;

import java.awt.image.BufferedImage;

public class ImageAnalyser {

    private static final int TOLERANCE = 10;
    private static final double BLACK_RATIO = 0.9d;

    /**
     * Checks if the given image is virtually black.
     *
     * @param img The BufferedImage to analyze
     * @return true if the image is virtually black, false otherwise
     */
    public boolean isVirtuallyBlack(final BufferedImage img) {
        final var width = img.getWidth();
        final var height = img.getHeight();
        final var total = width * height;
        var blackish = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final var rgb = img.getRGB(x, y);
                final var r = (rgb >> 16) & 0xFF;
                final var g = (rgb >> 8) & 0xFF;
                final var b = rgb & 0xFF;

                if (r <= TOLERANCE && g <= TOLERANCE && b <= TOLERANCE) {
                    blackish++;
                }
            }
        }

        final var ratio = (blackish * 1.0) / total;
        return ratio >= BLACK_RATIO;
    }
}

