package ch.epfl.gui;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    public static Image convert(LcdImage image) {
        int width = image.width();
        int height = image.height();
        WritableImage wi = new WritableImage(width, height);
        PixelWriter writer = wi.getPixelWriter();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                writer.setArgb(i, j, COLOR_MAP[image.get(i, j)]);
            }
        }
        
        return wi;
    }
}
