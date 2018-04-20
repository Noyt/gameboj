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

        int[] a2 = { 0b01111111_00000000_11111111_00000000,
                0b11110000_01010101_11110000_11111111 };
        Builder b2 = new Builder(64);
        fillBuilder(a2, b2);
        BitVector test2 = b2.build();
        assertEquals(
                "1111000001010101111100001111111101111111000000001111111100000000",
                test2.toString());

        int[] a3 = {};
        Builder b3 = new Builder(0);
        fillBuilder(a3, b3);
        BitVector test3 = b3.build();
        assertEquals("", test3.toString());
    }

    @Test
    void notWorksForValidValue() {
        Builder testBuilder = new Builder(96);
        int[] array = { -1, 0, -1 };
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();

        Builder testBuilderNot = new Builder(96);
        int[] arrayNot = { 0, -1, 0 };
        fillBuilder(arrayNot, testBuilderNot);
        BitVector vectNot = testBuilderNot.build();
        assertEquals(vect.not().toString(), vectNot.toString());
    }

    @Test
    void equalsWorksForValidValue() {
        Builder testBuilder = new Builder(64);
        int[] array = {-1, 0};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilder2 = new Builder(64);
        fillBuilder(array, testBuilder2);
        BitVector vect2 = testBuilder2.build();
        
        assertEquals(true, vect.equals(vect2));
    }
    
    @Test
    void equalsWorksForInvalidSize() {
        Builder testBuilder = new Builder(64);
        int[] array = {-1, 0};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilder2 = new Builder(96);
        int[] array2 = {0, -1, -1};
        fillBuilder(array2, testBuilder2);
        BitVector vect2 = testBuilder2.build();
        
        assertEquals(false, vect.equals(vect2));
    }
    
    @Test
    void equalsWorksForNonEqualVector() {
        Builder testBuilder = new Builder(64);
        int[] array = {-1, 0};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilder2 = new Builder(64);
        int[] array2 = {0, -1};
        fillBuilder(array2, testBuilder2);
        BitVector vect2 = testBuilder2.build();
        
        assertEquals(false, vect.equals(vect2));
    }

    @Test
    void testBitWorksForValidValues() {
        int[] a1 = { 0b00001111_00000000_11111111_10101010,
                0b01010101_11111111_00001111_00000001 };
        Builder b1 = new Builder(96);
        fillBuilder(a1, b1);
        BitVector v1 = b1.build();

        assertEquals(false, v1.testBit(0));
        assertEquals(true, v1.testBit(3));
        assertEquals(false, v1.testBit(31));
        assertEquals(true, v1.testBit(32));
        assertEquals(true, v1.testBit(50));
        assertEquals(false, v1.testBit(85));
    }

    @Test
    void shiftWorksForValidValues() {
        int[] a1 = { 0b00001111_00000000_11111111_10101010,
                0b01010101_11111111_00001111_00000001 };
        Builder b1 = new Builder(64);
        fillBuilder(a1, b1);
        BitVector v1 = b1.build();

        assertEquals(
                "1000011110000000100001111000000001111111110101010000000000000000",
                v1.shift(15).toString());
    }
    
    @Test
    void andWorksForValidValue() {
        Builder testBuilder = new Builder(64);
        int[] array = {0b11111111_00001111_00000000_11110101, 0b11111111_11111111_11110000_00000001};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilder2 = new Builder(64);
        int[] array2 = {0b00000000_11110000_11111111_00001011, 0b00000000_11111111_11110000_11111111};
        fillBuilder(array2, testBuilder2);
        BitVector vect2 = testBuilder2.build();
        
        Builder testBuilder3 = new Builder(64);
        int[] array3 = {0b00000000_00000000_00000000_00000001, 0b00000000_11111111_11110000_00000001};
        fillBuilder(array3, testBuilder3);
        BitVector vect3 = testBuilder3.build();
        
        
        assertEquals(true, vect3.equals(vect2.and(vect)));
    }
    
    @Test
    void orWorksForValidValue() {
        Builder testBuilder = new Builder(64);
        int[] array = {0b11111111_00001111_00000000_11110101, 0b11111111_11111111_11110000_00000001};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilder2 = new Builder(64);
        int[] array2 = {0b00000000_11110000_11111111_00001011, 0b00000000_11111111_11110000_11111111};
        fillBuilder(array2, testBuilder2);
        BitVector vect2 = testBuilder2.build();
        
        Builder testBuilder3 = new Builder(64);
        int[] array3 = {0b11111111_11111111_11111111_11111111, 0b11111111_11111111_11110000_11111111};
        fillBuilder(array3, testBuilder3);
        BitVector vect3 = testBuilder3.build();
        
        
        assertEquals(true, vect3.equals(vect2.or(vect)));
    }  
}
