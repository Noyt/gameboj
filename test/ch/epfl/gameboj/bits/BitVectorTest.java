package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.BitVector.Extraction;

class BitVectorTest {

    @Test
    void extractAndBuilderWorksForValidValue() {
        int[] array = { -1, 0 };
        Builder testBuilder = new Builder(64);
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                testBuilder.setByte(i * 4 + j,
                        Bits.extract(array[i], j * Byte.SIZE, Byte.SIZE));
            }
        }
        BitVector test = testBuilder.build();

        assertThrows(IllegalStateException.class,
                () -> testBuilder.setByte(0, 0));

        assertEquals(0b11111111_11111111_11110000_00000000,
                test.extractP(-12, 32, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0b11111111_11111111_11110000_00000000,
                test.extractP(-12, 32, Extraction.WRAPPEED)[0]);
        assertEquals(0b11111111_11111111_11111111_11111110,
                test.extractP(-1, 32, Extraction.WRAPPEED)[0]);

        int[] array2 = { -1, 0, 1431655765 };
        Builder testBuilder2 = new Builder(96);
        for (int i = 0; i < array2.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                testBuilder2.setByte(i * 4 + j,
                        Bits.extract(array2[i], j * Byte.SIZE, Byte.SIZE));
            }
        }

        BitVector test2 = testBuilder2.build();
        assertEquals(0b10101010_10101010_10101000_00000000,
                test2.extractP(-43, 64, Extraction.WRAPPEED)[0]);
        assertEquals(0b11111111_11111111_11111010_10101010,
                test2.extractP(-43, 64, Extraction.WRAPPEED)[1]);
        assertEquals(0, test2.extractP(-43, 64, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0b11111111_11111111_11111000_00000000,
                test2.extractP(-43, 64, Extraction.ZERO_EXTENDED)[1]);

        assertEquals(0, test2.extractP(-64, 128, Extraction.WRAPPEED)[0]);
        assertEquals(1431655765,
                test2.extractP(-64, 128, Extraction.WRAPPEED)[1]);
        assertEquals(-1, test2.extractP(-64, 128, Extraction.WRAPPEED)[2]);
        assertEquals(0, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[1]);
        assertEquals(-1, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[2]);
    }
}
