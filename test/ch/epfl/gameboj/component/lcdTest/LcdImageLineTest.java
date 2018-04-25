package ch.epfl.gameboj.component.lcdTest;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.lcd.LcdImageLine;

public class LcdImageLineTest {

    private void fillBitVectorBuilder(int[] array, Builder b) {
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                b.setByte(i * 4 + j,
                        Bits.extract(array[i], j * Byte.SIZE, Byte.SIZE));
            }
        }
    }

    private void fillLcdBuilder(int[] msbArray, int[] lsbArray,
            LcdImageLine.Builder b) {
        Preconditions.checkArgument(msbArray.length == lsbArray.length);

        for (int i = 0; i < msbArray.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                b.setBytes(i * 4 + j,
                        Bits.extract(msbArray[i], j * Byte.SIZE, Byte.SIZE),
                        Bits.extract(lsbArray[i], j * Byte.SIZE, Byte.SIZE));
            }
        }
    }

    @Test
    void mapColorsWorksForValidValue() {
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = { 0b00110011_00110011_00110011_11000011 };
        fillBitVectorBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();

        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = { 0b01010101_01010101_01010101_10100101 };
        fillBitVectorBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();

        BitVector opacity = new BitVector(32);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);

        Builder msbResultBuilder = new Builder(32);
        int[] arrayMsbResult = { 0b11001100_11001100_11001100_00111100 };
        fillBitVectorBuilder(arrayMsbResult, msbResultBuilder);
        BitVector msbResult = msbResultBuilder.build();

        Builder lsbResultBuilder = new Builder(32);
        int[] arrayLsbResult = { 0b10101010_10101010_10101010_01011010 };
        fillBitVectorBuilder(arrayLsbResult, lsbResultBuilder);
        BitVector lsbResult = lsbResultBuilder.build();

        assertEquals(msbResult, lcd.mapColors((byte) 0b00_01_10_11).msb());
        assertEquals(lsbResult, lcd.mapColors((byte) 0b00_01_10_11).lsb());
    }

    @Test
    void mapColorsWorksForValidValue2() {
        Builder msbBuilder = new Builder(64);
        int[] arrayMsb = { 0b00110011_00110011_00110011_11000011,
                0b00110011_00110011_00110011_11000011 };
        fillBitVectorBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();

        Builder lsbBuilder = new Builder(64);
        int[] arrayLsb = { 0b01010101_01010101_01010101_10100101,
                0b01010101_01010101_01010101_10100101 };
        fillBitVectorBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();

        BitVector opacity = new BitVector(64);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);

        Builder msbResultBuilder = new Builder(64);
        int[] arrayMsbResult = { 0b10101010_10101010_10101010_01011010,
                0b10101010_10101010_10101010_01011010 };
        fillBitVectorBuilder(arrayMsbResult, msbResultBuilder);
        BitVector msbResult = msbResultBuilder.build();

        Builder lsbResultBuilder = new Builder(64);
        int[] arrayLsbResult = { 0b11001100_11001100_11001100_00111100,
                0b11001100_11001100_11001100_00111100 };
        fillBitVectorBuilder(arrayLsbResult, lsbResultBuilder);
        BitVector lsbResult = lsbResultBuilder.build();

        assertEquals(msbResult, lcd.mapColors((byte) 0b00_10_01_11).msb());
        assertEquals(lsbResult, lcd.mapColors((byte) 0b00_10_01_11).lsb());
    }

    @Test
    void mapColorsWorksForNoChangeColor() {
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = { 0b00110011_00110011_00110011_11000011 };
        fillBitVectorBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();

        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = { 0b01010101_01010101_01010101_10100101 };
        fillBitVectorBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();

        BitVector opacity = new BitVector(32);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);

        assertEquals(msb, lcd.mapColors((byte) 0b11_10_01_00).msb());
        assertEquals(lsb, lcd.mapColors((byte) 0b11_10_01_00).lsb());
    }

    @Test
    void belowWorksForValidValue() {
        //TODO doubler les tableaux maybe ?
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = { 0b10110001_10110001_10110001_10110001 };
        fillBitVectorBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();

        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = { 0b01110110_01110110_01110110_01110110 };
        fillBitVectorBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();

        BitVector opacity = new BitVector(32);
        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);

        Builder msbBuilder2 = new Builder(32);
        int[] arrayMsb2 = { 0b00101101_00101101_00101101_00101101 };
        fillBitVectorBuilder(arrayMsb2, msbBuilder2);
        BitVector msb2 = msbBuilder2.build();

        Builder lsbBuilder2 = new Builder(32);
        int[] arrayLsb2 = { 0b01001111_01001111_01001111_01001111 };
        fillBitVectorBuilder(arrayLsb2, lsbBuilder2);
        BitVector lsb2 = lsbBuilder2.build();

        Builder opacityBuilder = new Builder(32);
        int[] arrayOpacity = { 0b01001010_00101100_10110111_00111111 };
        fillBitVectorBuilder(arrayOpacity, opacityBuilder);
        BitVector opacity2 = opacityBuilder.build();
        
        LcdImageLine lcd2 = new LcdImageLine(msb2, lsb2, opacity2);

        Builder trueOpacityBuilder = new Builder(32);
        int[] trueArrayOpacity = { 0b00101100_00101100_00101100_00101100 };
        fillBitVectorBuilder(trueArrayOpacity, trueOpacityBuilder);
        BitVector trueOpacity = trueOpacityBuilder.build();

        Builder msbResultBuilder = new Builder(32);
        int[] arrayMsbResult = { 0b00100001_00100001_00100001_00100001 };
        fillBitVectorBuilder(arrayMsbResult, msbResultBuilder);
        BitVector msbResult = msbResultBuilder.build();

        Builder lsbResultBuilder = new Builder(32);
        int[] arrayLsbResult = { 0b01100111_01100111_01100111_01100111 };
        fillBitVectorBuilder(arrayLsbResult, lsbResultBuilder);
        BitVector lsbResult = lsbResultBuilder.build();

        Builder opacityResultBuilder = new Builder(32);
        int[] arrayResultOpacity = { 0b01101110_00101100_10111111_00111111 };
        fillBitVectorBuilder(arrayResultOpacity, opacityResultBuilder);
        BitVector opacityResult = opacityResultBuilder.build();
        LcdImageLine lcdResult = new LcdImageLine(msbResult, lsbResult,
                opacityResult);

        assertEquals(lcdResult, lcd2.below(lcd, trueOpacity));
    }

    @Test
    void LcdImageLineBuilderWorksForValidValues() {
        LcdImageLine.Builder b = new LcdImageLine.Builder(64);
        int[] msbArray = { 0b00001001_11000000_00000101_00000001,
                0b11001100_10101010_00010001_11000011 };
        int[] lsbArray = { 0b00000001_00000101_00000000_00001011,
                0b10111011_10001000_01010111_00010010 };

        fillLcdBuilder(msbArray, lsbArray, b);
        LcdImageLine lcdIm1 = b.build();

        Builder msbResult = new Builder(64);
        Builder lsbResult = new Builder(64);
        fillBitVectorBuilder(msbArray, msbResult);
        fillBitVectorBuilder(lsbArray, lsbResult);

        int[] opacityR = { 0b00001001_11000101_00000101_00001011, 
                0b11111111_10101010_01010111_11010011 };
        Builder opacityResult = new Builder(64);
        fillBitVectorBuilder(opacityR, opacityResult);

        LcdImageLine result = new LcdImageLine(msbResult.build(),
                lsbResult.build(), opacityResult.build());

        assertEquals(result, lcdIm1);
    }
    
    @Test
    void joinWorksForValidValue() {
        Builder msbBuilder = new Builder(32);
        int[] arrayMsb = { 0b10110001_10110001_10110001_10110001 };
        fillBitVectorBuilder(arrayMsb, msbBuilder);
        BitVector msb = msbBuilder.build();

        Builder lsbBuilder = new Builder(32);
        int[] arrayLsb = { 0b01110110_01110110_01110110_01110110 };
        fillBitVectorBuilder(arrayLsb, lsbBuilder);
        BitVector lsb = lsbBuilder.build();
        
        Builder opacityBuilder = new Builder(32);
        int[] arrayOpacity = { 0b00101100_00101100_00101100_00101100 };
        fillBitVectorBuilder(arrayOpacity, opacityBuilder);
        BitVector opacity = opacityBuilder.build();

        LcdImageLine lcd = new LcdImageLine(msb, lsb, opacity);

        
        Builder msbBuilder2 = new Builder(32);
        int[] arrayMsb2 = { 0b00101101_00101101_00101101_00101101 };
        fillBitVectorBuilder(arrayMsb2, msbBuilder2);
        BitVector msb2 = msbBuilder2.build();

        Builder lsbBuilder2 = new Builder(32);
        int[] arrayLsb2 = { 0b01001111_01001111_01001111_01001111 };
        fillBitVectorBuilder(arrayLsb2, lsbBuilder2);
        BitVector lsb2 = lsbBuilder2.build();

        Builder opacityBuilder2 = new Builder(32);
        int[] arrayOpacity2 = { 0b11010110_10101110_00110110_10101000};
        fillBitVectorBuilder(arrayOpacity2, opacityBuilder2);
        BitVector opacity2 = opacityBuilder2.build();
        
        LcdImageLine lcd2 = new LcdImageLine(msb2, lsb2, opacity2);
        
        
        Builder msbBuilder3 = new Builder(32);
        int[] arrayMsb3 = { 0b00101101_00101101_00100001_10110001 };
        fillBitVectorBuilder(arrayMsb3, msbBuilder3);
        BitVector msb3 = msbBuilder3.build();

        Builder lsbBuilder3 = new Builder(32);
        int[] arrayLsb3 = { 0b01001111_01001111_01000110_01110110 };
        fillBitVectorBuilder(arrayLsb3, lsbBuilder3);
        BitVector lsb3 = lsbBuilder3.build();
        
        Builder opacityBuilder3 = new Builder(32);
        int[] arrayOpacity3 = { 0b11010110_10101110_00111100_00101100};
        fillBitVectorBuilder(arrayOpacity3, opacityBuilder3);
        BitVector opacity3 = opacityBuilder3.build();

        LcdImageLine lcd3 = new LcdImageLine(msb3, lsb3, opacity3);
        
        assertEquals(lcd3, lcd.join(lcd2, 12));
    }
}
