package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.BitVector.Extraction;

class BitVectorTest {

    private void fillBuilder(int[] array, Builder b) {
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                b.setByte(i * 4 + j,
                        Bits.extract(array[i], j * Byte.SIZE, Byte.SIZE));
            }
        }
    }

    @Test
    void extractAndBuilderWorksForValidValue() {
        int[] array = { -1, 0 };
        Builder testBuilder = new Builder(64);
        fillBuilder(array, testBuilder);
        BitVector test = testBuilder.build();

        assertThrows(IllegalStateException.class,
                () -> testBuilder.setByte(0, 0));

        assertEquals(0b11111111_11111111_11110000_00000000,
                test.extractP(-12, 32, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0b11111111_11111111_11110000_00000000,
                test.extractP(-12, 32, Extraction.WRAPPED)[0]);
        assertEquals(0b11111111_11111111_11111111_11111110,
                test.extractP(-1, 32, Extraction.WRAPPED)[0]);

        int[] array2 = { -1, 0, 1431655765 };
        Builder testBuilder2 = new Builder(96);
        fillBuilder(array2, testBuilder2);

        BitVector test2 = testBuilder2.build();
        assertEquals(0b10101010_10101010_10101000_00000000,
                test2.extractP(-43, 64, Extraction.WRAPPED)[0]);
        assertEquals(0b11111111_11111111_11111010_10101010,
                test2.extractP(-43, 64, Extraction.WRAPPED)[1]);
        assertEquals(0, test2.extractP(-43, 64, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0b11111111_11111111_11111000_00000000,
                test2.extractP(-43, 64, Extraction.ZERO_EXTENDED)[1]);

        assertEquals(0, test2.extractP(-64, 128, Extraction.WRAPPED)[0]);
        assertEquals(1431655765,
                test2.extractP(-64, 128, Extraction.WRAPPED)[1]);
        assertEquals(-1, test2.extractP(-64, 128, Extraction.WRAPPED)[2]);
        assertEquals(0, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[0]);
        assertEquals(0, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[1]);
        assertEquals(-1, test2.extractP(-64, 128, Extraction.ZERO_EXTENDED)[2]);
    }

    @Test
    void toStringWorksForValidValues() {
        int[] a = { 0b00000000_10101010_00000000_11001100 };
        Builder b = new Builder(32);
        fillBuilder(a, b);
        BitVector test = b.build();
        assertEquals("00000000101010100000000011001100", test.toString());

        int[] a2 = { 0b11111111_00000000_11111111_00000000,
                0b11110000_01010101_11110000_11111111 };
        Builder b2 = new Builder(64);
        fillBuilder(a2, b2);
        BitVector test2 = b2.build();
        assertEquals(
                "1111111100000000111111110000000011110000010101011111000011111111",
                test2.toString());
        
        int[] a3 = {};
        Builder b3 = new Builder(0);
        fillBuilder(a3, b3);
        BitVector test3 = b3.build();
        assertEquals("", test3.toString());
    }
}
