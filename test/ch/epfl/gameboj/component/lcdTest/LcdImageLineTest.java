package ch.epfl.gameboj.component.lcdTest;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.lcd.LcdImageLine;

public class LcdImageLineTest {

    
    private void fillBuilder(int[] array, Builder b) {
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                b.setByte(i * 4 + j,
                        Bits.extract(array[i], j * Byte.SIZE, Byte.SIZE));
            }
        }
    }
    
    @Test
    void mapColorsWorksForValidValue() {
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = {0b00110011_00110011_00110011_11000011};
        fillBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();
        
        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = {0b01010101_01010101_01010101_10100101};
        fillBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();
        
        BitVector opacity = new BitVector(32);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);
        
        Builder msbResultBuilder = new Builder(32);
        int[] arrayMsbResult = {0b11001100_11001100_11001100_00111100};
        fillBuilder(arrayMsbResult, msbResultBuilder);
        BitVector msbResult = msbResultBuilder.build();
        
        Builder lsbResultBuilder = new Builder(32);
        int[] arrayLsbResult = {0b10101010_10101010_10101010_01011010};
        fillBuilder(arrayLsbResult, lsbResultBuilder);
        BitVector lsbResult = lsbResultBuilder.build();
        
        assertEquals(msbResult, lcd.mapColors((byte) 0b00_01_10_11).msb());
        assertEquals(lsbResult, lcd.mapColors((byte) 0b00_01_10_11).lsb());
    }
    
    @Test
    void belowWorksForValidValue() {
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = {0b10110001_10110001_10110001_10110001};
        fillBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();
        
        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = {0b01110110_01110110_01110110_01110110};
        fillBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();
        
        BitVector opacity = new BitVector(32);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);
        
        
        
        Builder msbBuilder2 = new Builder(32);
        int[] arrayMsb2 = {0b00101101_00101101_00101101_00101101};
        fillBuilder(arrayMsb2, msbBuilder2);
        BitVector msb2 = msbBuilder2.build();
        
        Builder lsbBuilder2 = new Builder(32);
        int[] arrayLsb2 = {0b01001111_01001111_01001111_01001111};
        fillBuilder(arrayLsb2, lsbBuilder2);
        BitVector lsb2 = lsbBuilder2.build();
        
        BitVector opacity2 = new BitVector(32);
        LcdImageLine lcd2 = new LcdImageLine(msb2, lsb2, opacity2);

        
        
        Builder opacityBuilder = new Builder(32);
        int[] arrayOpacity = {0b00101100_00101100_00101100_00101100};
        fillBuilder(arrayOpacity, opacityBuilder);
        BitVector trueOpacity = opacityBuilder.build();


        
        Builder msbResultBuilder = new Builder(32);
        int[] arrayMsbResult = {0b00100001_00100001_00100001_00100001};
        fillBuilder(arrayMsbResult, msbResultBuilder);
        BitVector msbResult = msbResultBuilder.build();
        
        Builder lsbResultBuilder = new Builder(32);
        int[] arrayLsbResult = {0b01100111_01100111_01100111_01100111};
        fillBuilder(arrayLsbResult, lsbResultBuilder);
        BitVector lsbResult = lsbResultBuilder.build();
        
        BitVector opacityResult = new BitVector(32, true);
        LcdImageLine lcdResult = new LcdImageLine(msbResult, lsbResult, opacityResult);
        
        assertEquals(lcdResult.msb(), lcd2.below(lcd, trueOpacity).msb());
        assertEquals(lcdResult.lsb(), lcd2.below(lcd, trueOpacity).lsb());
    }
}
