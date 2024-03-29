package ch.epfl.gameboj.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public final class DebugMain3 {
    private static final int[] COLOR_MAP = new int[] {
      0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    public static void main(String[] args) throws IOException {
      File romFile = new File(args[0]);
      long cycles = 30_000_000;

      GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
      gb.runUntil(cycles);
      gb.joypad().keyPressed(Key.START);
      gb.runUntil(cycles + (1L << 20));
      gb.joypad().keyReleased(Key.START);   
      gb.runUntil(cycles + 2 * (1L << 20));
      //gb.joypad().keyPressed(Key.A);
      gb.runUntil(cycles + (long) Math.floor(2.5 * (1L << 20)));
      

      LcdImage li = gb.lcdController().currentImage();
      BufferedImage i =
        new BufferedImage(li.width(),
              li.height(),
              BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < li.height(); ++y)
        for (int x = 0; x < li.width(); ++x)
      i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
      ImageIO.write(i, "png", new File("gb.png"));
    }
  }