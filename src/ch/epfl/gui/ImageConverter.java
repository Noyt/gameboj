package ch.epfl.gui;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00
    };

    public static Image convert(LcdImage image) {
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
