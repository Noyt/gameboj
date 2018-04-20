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
        for (int i = 0; i < array2.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                testBuilder2.setByte(i * 4 + j,
                        Bits.extract(array2[i], j * Byte.SIZE, Byte.SIZE));
            }
        }

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
    void notWorksForValidValue() {
        Builder testBuilder = new Builder(96);
        int[] array = {-1, 0, -1};
        fillBuilder(array, testBuilder);
        BitVector vect = testBuilder.build();
        
        Builder testBuilderNot = new Builder(96);
        int[] arrayNot = {0, -1, 0};
        fillBuilder(arrayNot, testBuilderNot);
        BitVector vectNot = testBuilderNot.build();
        
        assertEquals(vect.not(), vectNot);   
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
