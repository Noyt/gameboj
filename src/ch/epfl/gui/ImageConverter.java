package ch.epfl.gui;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A class used to convert an LcdImage to his equivalent javafx Image
 * 
 * @author Sophie du Couédic (260007)
 * @author Arnaud Robert (287964)
 *
 */
public final class ImageConverter {

    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF,
            0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 };
    
    /**
     * The method used to convert an LcdImage to his equivalent javafx Image
     * 
     * @param image
     *            the image to convert
     * @return an image of type javafx.scene.image.Image
     */
    public static Image convert(LcdImage image) {

        if (image == null) {
            return null;
        }

        int width = image.width();
        int height = image.height();
        WritableImage wi = new WritableImage(width, height);

        PixelWriter writer = wi.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                writer.setArgb(i, j, COLOR_MAP[image.get(i, j)]);
            }
        }

        return wi;
    }
}
